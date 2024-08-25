package com.aurora.store.data.work

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkInfo.Companion.STOP_REASON_CANCELLED_BY_APP
import androidx.work.WorkInfo.Companion.STOP_REASON_USER
import androidx.work.WorkerParameters
import com.aurora.extensions.copyTo
import com.aurora.extensions.isPAndAbove
import com.aurora.extensions.isQAndAbove
import com.aurora.extensions.isSAndAbove
import com.aurora.extensions.requiresObbDir
import com.aurora.gplayapi.helpers.PurchaseHelper
import com.aurora.store.data.installer.AppInstaller
import com.aurora.store.data.model.Algorithm
import com.aurora.store.data.model.DownloadInfo
import com.aurora.store.data.model.DownloadStatus
import com.aurora.store.data.model.ProxyInfo
import com.aurora.store.data.model.Request
import com.aurora.store.data.network.IProxyHttpClient
import com.aurora.store.data.providers.AuthProvider
import com.aurora.store.data.room.download.Download
import com.aurora.store.data.room.download.DownloadDao
import com.aurora.store.util.CertUtil
import com.aurora.store.util.DownloadWorkerUtil
import com.aurora.store.util.NotificationUtil
import com.aurora.store.util.PathUtil
import com.aurora.store.util.Preferences
import com.aurora.store.util.Preferences.PREFERENCE_PROXY_ENABLED
import com.aurora.store.util.Preferences.PREFERENCE_PROXY_INFO
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.net.URL
import java.security.DigestInputStream
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection
import kotlin.properties.Delegates
import com.aurora.gplayapi.data.models.File as GPlayFile

/**
 * An expedited long-running worker to download and trigger installation for given apps.
 *
 * Avoid using this worker directly and prefer using [DownloadWorkerUtil] instead.
 */
@HiltWorker
class DownloadWorker @AssistedInject constructor(
    private val downloadDao: DownloadDao,
    private val gson: Gson,
    private val appInstaller: AppInstaller,
    private val authProvider: AuthProvider,
    private val httpClient: IProxyHttpClient,
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private lateinit var download: Download
    private lateinit var notificationManager: NotificationManager
    private lateinit var icon: Bitmap
    private lateinit var purchaseHelper: PurchaseHelper

    private val proxy: Proxy? = getProxy()

    private val notificationId = 200

    private var downloading = false
    private var totalBytes by Delegates.notNull<Long>()
    private var totalProgress = 0
    private var downloadedBytes = 0L

    private val tag = DownloadWorker::class.java.simpleName

    override suspend fun doWork(): Result {
        // Bail out immediately if authData is not valid
        if (!authProvider.isSavedAuthDataValid()) {
            Log.e(tag, "AuthData is not valid, exiting!")
            onFailure()
            return Result.failure()
        }

        // Try to parse input data into a valid app
        try {
            val downloadData = inputData.getString(DownloadWorkerUtil.DOWNLOAD_DATA)
            download = gson.fromJson(downloadData, Download::class.java)

            val bitmap = BitmapFactory.decodeStream(withContext(Dispatchers.IO) {
                URL(download.iconURL).openStream()
            })
            icon = Bitmap.createScaledBitmap(bitmap, 96, 96, true)
        } catch (exception: Exception) {
            Log.e(tag, "Failed to parse download data", exception)
            onFailure()
            return Result.failure()
        }

        // Set work/service to foreground on < Android 12.0
        setForeground(getForegroundInfo())

        // Purchase the app (free apps needs to be purchased too)
        purchaseHelper = PurchaseHelper(authProvider.authData!!)
            .using(httpClient)

        notificationManager =
            appContext.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        // Bail out if file list is empty
        download.fileList = download.fileList.ifEmpty {
            purchase(download.packageName, download.versionCode, download.offerType)
        }
        if (download.fileList.isEmpty()) {
            Log.i(tag, "Nothing to download!")
            onFailure()
            return Result.failure()
        }

        // Create dirs & generate download request for files and shared libs (if any)
        PathUtil.getAppDownloadDir(appContext, download.packageName, download.versionCode).mkdirs()
        if (download.fileList.requiresObbDir()) {
            PathUtil.getObbDownloadDir(download.packageName).mkdirs()
        }

        val requestList = mutableListOf<Request>()
        if (download.sharedLibs.isNotEmpty()) {
            // Purchase and append shared libs data to existing request
            download.sharedLibs.forEach {
                PathUtil.getLibDownloadDir(
                    appContext,
                    download.packageName,
                    download.versionCode,
                    it.packageName
                ).mkdirs()
                it.fileList = it.fileList.ifEmpty { purchase(it.packageName, it.versionCode, 0) }
                requestList.addAll(getDownloadRequest(it.fileList, it.packageName))
            }
        }
        requestList.addAll(getDownloadRequest(download.fileList, null))

        // Update data for notification
        download.totalFiles = requestList.size
        totalBytes = requestList.sumOf { it.size }

        // Update database with all latest purchases
        downloadDao.updateFiles(download.packageName, download.fileList)
        downloadDao.updateSharedLibs(download.packageName, download.sharedLibs)

        // Download and verify all files exists
        requestList.forEach { request ->
            downloading = true
            runCatching {
                downloadFile(request); download.downloadedFiles++
            }
                .onSuccess {
                    downloading = false
                }
                .onFailure {
                    Log.e(tag, "Failed to download ${download.packageName}", it)
                    downloading = false
                    onFailure()
                    return Result.failure()
                }
            while (downloading) {
                delay(1000)
                if (isStopped || downloadDao.getDownload(download.packageName).downloadStatus == DownloadStatus.CANCELLED) {
                    onFailure()
                    break
                }
            }
        }

        if (!requestList.all {
                it.file.exists()
            }) {
            Log.e(tag, "Downloaded files are missing")
            onFailure()
            return Result.failure()
        }

        // Mark download as completed
        onSuccess()
        return Result.success()
    }

    private suspend fun onSuccess() {
        withContext(NonCancellable) {
            Log.i(tag, "Finished downloading ${download.packageName}")
            notifyStatus(DownloadStatus.COMPLETED)

            try {
                appInstaller.getPreferredInstaller().install(download)
            } catch (exception: Exception) {
                Log.e(tag, "Failed to install ${download.packageName}", exception)
            }
        }
    }

    private suspend fun onFailure() {
        withContext(NonCancellable) {
            Log.i(tag, "Failed downloading ${download.packageName}")
            val cancelReasons = listOf(STOP_REASON_USER, STOP_REASON_CANCELLED_BY_APP)
            if (isSAndAbove() && stopReason in cancelReasons) {
                notifyStatus(DownloadStatus.CANCELLED)
            } else {
                notifyStatus(DownloadStatus.FAILED)
            }

            with(appContext.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager) {
                cancel(notificationId)
            }
        }
    }

    /**
     * Purchases the app to get the download URL of the required files
     * @param packageName The packageName of the app
     * @param versionCode Required version of the app
     * @param offerType Offer type of the app (free/paid)
     * @return A list of purchased files
     */
    private fun purchase(packageName: String, versionCode: Int, offerType: Int): List<GPlayFile> {
        // Android 9.0+ supports key rotation, so purchase with latest certificate's hash
        return if (isPAndAbove() && download.isInstalled) {
            purchaseHelper.purchase(
                packageName,
                versionCode,
                offerType,
                CertUtil.getEncodedCertificateHashes(appContext, download.packageName).last()
            )
        } else {
            purchaseHelper.purchase(packageName, versionCode, offerType)
        }
    }

    private fun getDownloadRequest(files: List<GPlayFile>, libPackageName: String?): List<Request> {
        val downloadList = mutableListOf<Request>()
        files.filter { it.url.isNotBlank() }.forEach {
            val file = when (it.type) {
                GPlayFile.FileType.BASE, GPlayFile.FileType.SPLIT -> {
                    PathUtil.getApkDownloadFile(
                        appContext,
                        download.packageName,
                        download.versionCode,
                        it,
                        libPackageName
                    )
                }

                GPlayFile.FileType.OBB, GPlayFile.FileType.PATCH -> {
                    PathUtil.getObbDownloadFile(download.packageName, it)
                }
            }
            downloadList.add(Request(it.url, file, it.size, it.sha1, it.sha256))
        }
        return downloadList
    }

    /**
     * Downloads the file from the given request.
     * Failed downloads aren't removed.
     * @param request A [Request] to download
     * @return A [Result] indicating whether the file was downloaded or not.
     */
    private suspend fun downloadFile(request: Request): Result {
        return withContext(Dispatchers.IO) {
            val algorithm = if (request.sha256.isBlank()) Algorithm.SHA1 else Algorithm.SHA256
            val expectedSha = if (algorithm == Algorithm.SHA1) request.sha1 else request.sha256

            // If file exists and sha matches the request, no need to download again
            if (request.file.exists() && validSha(request.file, expectedSha, algorithm)) {
                Log.i(tag, "${request.file} is already downloaded!")
                downloadedBytes += request.file.length()
                return@withContext Result.success()
            }

            try {
                val isNewFile = request.file.createNewFile()
                val connection = if (proxy != null) {
                    URL(request.url).openConnection(proxy) as HttpsURLConnection
                } else {
                    URL(request.url).openConnection() as HttpsURLConnection
                }

                if (!isNewFile) {
                    Log.i(tag, "${request.file} has an unfinished download, resuming!")
                    downloadedBytes += request.file.length()
                    connection.setRequestProperty("Range", "bytes=${request.file.length()}-")
                }

                connection.inputStream.use { input ->
                    FileOutputStream(request.file, !isNewFile).use {
                        input.copyTo(it, request.size).collect { p -> onProgress(p) }
                    }
                }

                // Ensure downloaded file matches expected sha
                if (validSha(request.file, expectedSha, algorithm)) {
                    return@withContext Result.success()
                } else {
                    Log.e(tag, "Incorrect hash for ${request.file}")
                    throw Exception("Incorrect hash")
                }

            } catch (exception: Exception) {
                Log.e(tag, "Failed to download ${request.file}!", exception)
                notifyStatus(DownloadStatus.FAILED)
                return@withContext Result.failure()
            }
        }
    }

    /**
     * Updates the progress data of the download in the local database and notifies user.
     * @param downloadInfo An instance of [DownloadInfo]
     */
    private suspend fun onProgress(downloadInfo: DownloadInfo) {
        if (!isStopped && !download.isFinished) {
            downloadedBytes += downloadInfo.bytesCopied

            val progress = (downloadedBytes * 100 / totalBytes).toInt()
            val bytesRemaining = totalBytes - downloadedBytes
            val speed = if (downloadInfo.speed == 0L) {
                1
            } else {
                downloadInfo.speed
            }

            // Individual file progress can be negligible in contrast to total progress
            // Only notify the UI if progress is greater or speed has changed to avoid being rate-limited by Android
            if (progress > totalProgress || speed != download.speed) {
                download.apply {
                    this.progress = progress
                    this.speed = downloadInfo.speed
                    this.timeRemaining = bytesRemaining / speed * 1000
                }

                // Update progress every 5% to avoid noise
                if (totalProgress % 5 == 0) {
                    downloadDao.updateProgress(
                        download.packageName,
                        download.progress,
                        download.speed,
                        download.timeRemaining
                    )

                    notifyStatus(DownloadStatus.DOWNLOADING, notificationId)
                }

                totalProgress = progress
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = if (this::download.isInitialized) {
            NotificationUtil.getDownloadNotification(appContext, download, id, icon)
        } else {
            NotificationUtil.getDownloadNotification(appContext)
        }
        return if (isQAndAbove()) {
            ForegroundInfo(notificationId, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }

    /**
     * Notifies the user of the current status of the download.
     * @param status Current [DownloadStatus]
     * @param dID ID of the notification, defaults to hashCode of the download's packageName
     */
    private suspend fun notifyStatus(status: DownloadStatus, dID: Int = -1) {
        // Update status in database
        download.downloadStatus = status
        downloadDao.updateStatus(download.packageName, status)

        when (status) {
            DownloadStatus.CANCELLED -> return
            DownloadStatus.COMPLETED -> {
                // Mark progress as 100 manually to avoid race conditions
                download.progress = 100
                downloadDao.updateProgress(download.packageName, 100, 0, 0)
            }

            else -> {}
        }

        val notification = NotificationUtil.getDownloadNotification(appContext, download, id, icon)
        val notificationID = if (dID != -1) dID else download.packageName.hashCode()
        notificationManager.notify(notificationID, notification)
    }

    /**
     * Validates whether given file has the expected SHA hash sum.
     * @param file [File] to check
     * @param expectedSha Expected SHA hash sum
     * @param algorithm [Algorithm] of the SHA
     * @return A boolean whether the given file has the expected SHA or not.
     */
    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun validSha(file: File, expectedSha: String, algorithm: Algorithm): Boolean {
        return withContext(Dispatchers.IO) {
            val messageDigest = MessageDigest.getInstance(algorithm.value)
            DigestInputStream(file.inputStream(), messageDigest).use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var read = input.read(buffer, 0, DEFAULT_BUFFER_SIZE)
                while (read > -1) {
                    read = input.read(buffer, 0, DEFAULT_BUFFER_SIZE)
                }
            }
            val sha = messageDigest.digest().toHexString()
            return@withContext sha == expectedSha
        }
    }

    /**
     * Gets the current [Proxy] configuration.
     * @return An instance of [Proxy] if configured by the user, null otherwise
     */
    private fun getProxy(): Proxy? {
        val proxyEnabled = Preferences.getBoolean(appContext, PREFERENCE_PROXY_ENABLED)
        val proxyInfoString = Preferences.getString(appContext, PREFERENCE_PROXY_INFO)

        if (proxyEnabled && proxyInfoString.isNotBlank() && proxyInfoString != "{}") {
            val proxyInfo = gson.fromJson(proxyInfoString, ProxyInfo::class.java)

            val proxy = Proxy(
                if (proxyInfo.protocol == "SOCKS") Proxy.Type.SOCKS else Proxy.Type.HTTP,
                InetSocketAddress.createUnresolved(proxyInfo.host, proxyInfo.port)
            )

            val proxyUser = proxyInfo.proxyUser
            val proxyPassword = proxyInfo.proxyPassword

            if (!proxyUser.isNullOrBlank() && !proxyPassword.isNullOrBlank()) {
                Authenticator.setDefault(object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(proxyUser, proxyPassword.toCharArray())
                    }
                })
            }
            return proxy
        } else {
            Log.i(tag, "Proxy is disabled")
            return null
        }
    }
}
