/*
 * Aurora Store
 *  Copyright (C) 2021, Rahul Kumar Patel <whyorean@gmail.com>
 *
 *  Aurora Store is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Aurora Store is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.aurora.store.view.ui.preferences

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.aurora.extensions.isOAndAbove
import com.aurora.extensions.isRAndAbove
import com.aurora.extensions.runOnUiThread
import com.aurora.extensions.showDialog
import com.aurora.extensions.toast
import com.aurora.store.BuildConfig
import com.aurora.store.R
import com.aurora.store.data.installer.AMInstaller
import com.aurora.store.data.installer.AppInstaller
import com.aurora.store.data.installer.ServiceInstaller
import com.aurora.store.data.installer.ShizukuInstaller
import com.aurora.store.data.work.UpdateWorker
import com.aurora.store.util.CommonUtil
import com.aurora.store.util.Log
import com.aurora.store.util.PackageUtil
import com.aurora.store.util.PathUtil
import com.aurora.store.util.Preferences
import com.aurora.store.util.Preferences.PREFERENCE_UPDATES_CHECK
import com.aurora.store.util.isExternalStorageAccessible
import com.aurora.store.util.save
import com.aurora.store.view.custom.preference.AuroraListPreference
import com.topjohnwu.superuser.Shell
import rikka.shizuku.Shizuku


class DownloadPreference : PreferenceFragmentCompat() {
    private lateinit var startForStorageManagerResult: ActivityResultLauncher<Intent>
    private lateinit var startForPermissions: ActivityResultLauncher<String>

    private var downloadDirectoryPreference: Preference? = null
    private var downloadExternalPreference: SwitchPreferenceCompat? = null
    private var autoDeletePreference: SwitchPreferenceCompat? = null

    private var shizukuAlive = false
    private val shizukuAliveListener = Shizuku.OnBinderReceivedListener {
        Log.d("ShizukuInstaller Alive!")
        shizukuAlive = true
    }
    private val shizukuDeadListener = Shizuku.OnBinderDeadListener {
        Log.d("ShizukuInstaller Dead!")
        shizukuAlive = false
    }
    private val shizukuResultListener =
        Shizuku.OnRequestPermissionResultListener { _: Int, result: Int ->
            if (result == PackageManager.PERMISSION_GRANTED) {
                save(Preferences.PREFERENCE_INSTALLER_ID, 5)
                activity?.recreate()
            } else {
                showDialog(
                    R.string.action_installations,
                    R.string.installer_shizuku_unavailable
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startForStorageManagerResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val state = isRAndAbove() && Environment.isExternalStorageManager()
                if (state) {
                    downloadDirectoryPreference?.summary =
                        PathUtil.getExternalPath(requireContext())
                } else {
                    notifyPermissionState(isRAndAbove() && Environment.isExternalStorageManager())
                }
            }

        startForPermissions =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it) {
                    notifyPermissionState(it)
                } else {
                    downloadExternalPreference?.isChecked = false
                }
            }
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_download, rootKey)

        findPreference<SwitchPreferenceCompat>(PREFERENCE_UPDATES_CHECK)
            ?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toBoolean()) {
                    UpdateWorker.scheduleAutomatedCheck(requireContext())
                } else {
                    UpdateWorker.cancelAutomatedCheck(requireContext())
                }
                true
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Toolbar>(R.id.toolbar)?.apply {
            title = getString(R.string.pref_app_download)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        downloadDirectoryPreference = findPreference(Preferences.PREFERENCE_DOWNLOAD_DIRECTORY)
        downloadExternalPreference = findPreference(Preferences.PREFERENCE_DOWNLOAD_EXTERNAL)
        autoDeletePreference = findPreference(Preferences.PREFERENCE_AUTO_DELETE)

        downloadDirectoryPreference?.let { preference ->
            preference.summary = PathUtil.getExternalPath(requireContext())
            preference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { it, newValue ->
                    if (PathUtil.canWriteToDirectory(requireContext(), newValue.toString())) {
                        it.summary = newValue.toString()
                        save(Preferences.PREFERENCE_DOWNLOAD_DIRECTORY, newValue.toString())
                        true
                    } else {
                        toast(R.string.pref_download_directory_error)
                        false
                    }
                }
        }

        downloadExternalPreference?.let { switchPreferenceCompat ->
            switchPreferenceCompat.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    val checked = newValue.toString().toBoolean()

                    if (checked) {
                        if (isRAndAbove() && !Environment.isExternalStorageManager()) {
                            startForStorageManagerResult.launch(
                                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            )
                        }

                        if (!isRAndAbove() && !isExternalStorageAccessible(requireContext())) {
                            startForPermissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }
                    autoDeletePreference?.let {
                        if (checked) {
                            it.isEnabled = true
                        } else {
                            it.isEnabled = false
                            it.isChecked = true
                        }
                    }

                    true
                }
        }

        if (AppInstaller.hasShizuku(requireContext()) && isOAndAbove()) {
            Shizuku.addBinderReceivedListenerSticky(shizukuAliveListener)
            Shizuku.addBinderDeadListener(shizukuDeadListener)
            Shizuku.addRequestPermissionResultListener(shizukuResultListener)
        }

        val abandonPreference: Preference? =
            findPreference(Preferences.PREFERENCE_INSTALLATION_ABANDON_SESSION)

        abandonPreference?.let {
            it.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    CommonUtil.cleanupInstallationSessions(requireContext())
                    runOnUiThread {
                        requireContext().toast(R.string.toast_abandon_sessions)
                    }
                    false
                }
        }

        val installerPreference: AuroraListPreference? =
            findPreference(Preferences.PREFERENCE_INSTALLER_ID)

        installerPreference?.let {
            it.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    val selectedId = Integer.parseInt(newValue as String)
                    if (selectedId == 2) {
                        if (checkRootAvailability()) {
                            save(Preferences.PREFERENCE_INSTALLER_ID, selectedId)
                            true
                        } else {
                            showDialog(
                                R.string.action_installations,
                                R.string.installer_root_unavailable
                            )
                            false
                        }
                    } else if (selectedId == 3) {
                        if (checkServicesAvailability()) {
                            save(Preferences.PREFERENCE_INSTALLER_ID, selectedId)
                            true
                        } else {
                            showDialog(
                                R.string.action_installations,
                                R.string.installer_service_unavailable
                            )
                            false
                        }
                    } else if (selectedId == 4) {
                        if (checkAMAvailability()) {
                            save(Preferences.PREFERENCE_INSTALLER_ID, selectedId)
                            true
                        } else {
                            showDialog(
                                R.string.action_installations,
                                R.string.installer_am_unavailable
                            )
                            false
                        }
                    } else if (selectedId == 5) {
                        if (AppInstaller.hasShizuku(requireContext()) && isOAndAbove()) {
                            if (shizukuAlive && AppInstaller.hasShizukuPerm()) {
                                save(Preferences.PREFERENCE_INSTALLER_ID, selectedId)
                                true
                            } else if (shizukuAlive && !Shizuku.shouldShowRequestPermissionRationale()) {
                                Shizuku.requestPermission(9000)
                                false
                            } else {
                                showDialog(
                                    R.string.action_installations,
                                    R.string.installer_shizuku_unavailable
                                )
                                false
                            }
                        } else {
                            showDialog(
                                R.string.action_installations,
                                R.string.installer_shizuku_unavailable
                            )
                            false
                        }
                    } else {
                        save(Preferences.PREFERENCE_INSTALLER_ID, selectedId)
                        true
                    }
                }
        }
    }

    private fun notifyPermissionState(state: Boolean) {
        if (state) {
            toast(R.string.toast_permission_granted)
        } else {
            toast(R.string.permissions_denied)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        startForStorageManagerResult.unregister()
        startForPermissions.unregister()
        if (AppInstaller.hasShizuku(requireContext()) && isOAndAbove()) {
            Shizuku.removeBinderReceivedListener(shizukuAliveListener)
            Shizuku.removeBinderDeadListener(shizukuDeadListener)
            Shizuku.removeRequestPermissionResultListener(shizukuResultListener)
        }
    }

    private fun checkRootAvailability(): Boolean {
        return Shell.getShell().isRoot
    }

    private fun checkServicesAvailability(): Boolean {
        val isInstalled = PackageUtil.isInstalled(
            requireContext(),
            ServiceInstaller.PRIVILEGED_EXTENSION_PACKAGE_NAME
        )

        val isCorrectVersionInstalled =
            PackageUtil.isInstalled(
                requireContext(),
                ServiceInstaller.PRIVILEGED_EXTENSION_PACKAGE_NAME,
                if (BuildConfig.VERSION_CODE < 31) 8 else 9
            )

        return isInstalled && isCorrectVersionInstalled
    }

    private fun checkAMAvailability(): Boolean {
        return PackageUtil.isInstalled(
            requireContext(),
            AMInstaller.AM_PACKAGE_NAME
        ) or PackageUtil.isInstalled(
            requireContext(),
            AMInstaller.AM_DEBUG_PACKAGE_NAME
        )
    }

    private fun checkShizukuAvailability(): Boolean {
        return PackageUtil.isInstalled(requireContext(), ShizukuInstaller.SHIZUKU_PACKAGE_NAME) &&
                shizukuAlive && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }
}
