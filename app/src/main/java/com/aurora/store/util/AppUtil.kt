package com.aurora.store.util

import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import com.aurora.Constants
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.store.AuroraApp
import com.aurora.store.BuildConfig
import com.aurora.store.data.model.SelfUpdate
import com.aurora.store.data.network.HttpClient
import com.aurora.store.data.providers.AuthProvider
import com.aurora.store.data.providers.BlacklistProvider
import com.aurora.store.data.room.update.Update
import com.aurora.store.data.room.update.UpdateDao
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppUtil @Inject constructor(
    private val gson: Gson,
    private val authProvider: AuthProvider,
    private val updateDao: UpdateDao,
    private val blacklistProvider: BlacklistProvider,
    @ApplicationContext private val context: Context
) {
    private val tag : String = AppUtil::class.java.simpleName
    private val isExtendedUpdateEnabled : Boolean get() = Preferences.getBoolean(context, Preferences.PREFERENCE_UPDATES_EXTENDED)
    val updates = updateDao.updates()
        .map { list ->
            list.filter {
                it.isInstalled(context)
            }
        }
        .map { list ->
            if (!isExtendedUpdateEnabled) {
                list.filter {
                    it.hasValidCert
                }
            } else {
                list
            }
        }
        .stateIn(AuroraApp.scope, SharingStarted.WhileSubscribed(), null)

    suspend fun checkUpdates(tmpAuthData: AuthData? = null): List<Update> {
        Log.i(tag, "Checking for updates")
        val packageInfoMap = PackageUtil.getPackageInfoMap(context)
        val appUpdatesList = getFilteredInstalledApps(tmpAuthData, packageInfoMap).filter {
            val packageInfo = packageInfoMap[it.packageName]
            if (packageInfo != null) {
                it.versionCode.toLong() > PackageInfoCompat.getLongVersionCode(packageInfo)
            } else {
                false
            }
        }.toMutableList()

        if (canSelfUpdate(context)) {
            getSelfUpdate(context, gson)?.let {
                appUpdatesList.add(it)
            }
        }

        return appUpdatesList
            .map {
                Update.fromApp(context, it)
            }.also {
                // Cache the updates into the database
                updateDao.insertUpdates(it)
            }
    }

    suspend fun deleteUpdate(packageName: String) {
        updateDao.delete(packageName)
    }

    suspend fun getFilteredInstalledApps(
        tmpAuthData: AuthData? = null,
        packageInfoMap: MutableMap<String, PackageInfo>? = null
    ): List<App> {
        return withContext(Dispatchers.IO) {
            val appDetailsHelper = AppDetailsHelper(tmpAuthData?: authProvider.authData!!)
                .using(HttpClient.getPreferredClient(context))

            (packageInfoMap ?: PackageUtil.getPackageInfoMap(context)).keys.let { packages ->
                val filtersPackages = packages.filter {
                    !blacklistProvider.isBlacklisted(it)
                }

                appDetailsHelper.getAppByPackageName(filtersPackages)
                    .filter {
                        it.displayName.isNotEmpty()
                    }
                    .map {
                        it.isInstalled = true; it
                    }
            }
        }
    }

    private fun canSelfUpdate(context: Context): Boolean {
        return !CertUtil.isFDroidApp(context, BuildConfig.APPLICATION_ID) && !CertUtil.isAppGalleryApp(context, BuildConfig.APPLICATION_ID)
    }

    private suspend fun getSelfUpdate(context: Context, gson: Gson): App? {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    HttpClient.getPreferredClient(context).get(Constants.UPDATE_URL, mapOf())
                val selfUpdate =
                    gson.fromJson(String(response.responseBytes), SelfUpdate::class.java)

                if (selfUpdate.versionCode > BuildConfig.VERSION_CODE) {
                    if (CertUtil.isFDroidApp(context, BuildConfig.APPLICATION_ID)) {
                        if (selfUpdate.fdroidBuild.isNotEmpty()) {
                            return@withContext SelfUpdate.toApp(selfUpdate, context)
                        }
                    } else if (selfUpdate.auroraBuild.isNotEmpty()) {
                        return@withContext SelfUpdate.toApp(selfUpdate, context)
                    } else {
                        Log.e(tag, "Update file is missing!")
                        return@withContext null
                    }
                }
            } catch (exception: Exception) {
                Log.e(tag, "Failed to check self-updates", exception)
                return@withContext null
            }

            Log.i(tag, "No self-updates found!")
            return@withContext null
        }
    }

}
