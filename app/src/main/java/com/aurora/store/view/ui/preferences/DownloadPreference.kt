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
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.aurora.extensions.isIgnoringBatteryOptimizations
import com.aurora.extensions.isRAndAbove
import com.aurora.extensions.runOnUiThread
import com.aurora.extensions.showDialog
import com.aurora.extensions.toast
import com.aurora.store.MobileNavigationDirections
import com.aurora.store.R
import com.aurora.store.data.work.UpdateWorker
import com.aurora.store.util.CommonUtil
import com.aurora.store.util.PathUtil
import com.aurora.store.util.Preferences
import com.aurora.store.util.Preferences.INSTALLATION_ABANDON_SESSION
import com.aurora.store.util.Preferences.PREFERENCE_INSTALLATION_DEVICE_OWNER
import com.aurora.store.util.Preferences.PREFERENCE_INSTALLER_ID
import com.aurora.store.util.Preferences.PREFERENCE_UPDATES_AUTO
import com.aurora.store.util.Preferences.PREFERENCE_UPDATES_CHECK_INTERVAL
import com.aurora.store.util.isExternalStorageAccessible
import com.aurora.store.util.save
import com.aurora.store.view.custom.preference.AuroraListPreference
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadPreference : PreferenceFragmentCompat() {
    private lateinit var startForStorageManagerResult: ActivityResultLauncher<Intent>
    private lateinit var startForPermissions: ActivityResultLauncher<String>

    private var downloadExternalPreference: SwitchPreferenceCompat? = null
    private var downloadDirectoryPreference: EditTextPreference? = null
    private var updatesIntervalPreference: SeekBarPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updatesIntervalPreference = findPreference(PREFERENCE_UPDATES_CHECK_INTERVAL)

        startForStorageManagerResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val state = isRAndAbove() && Environment.isExternalStorageManager()
                if (state) {
                    downloadDirectoryPreference?.summary = PathUtil.getExternalPath(requireContext())
                } else {
                    notifyPermissionState(isRAndAbove() && Environment.isExternalStorageManager())
                }
            }

        startForPermissions = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                notifyPermissionState(it)
            } else {
                downloadExternalPreference?.isChecked = false
            }
        }

        updatesIntervalPreference?.isEnabled = context?.let { Preferences.getInteger(it, PREFERENCE_UPDATES_AUTO).toString() }!! != 0.toString()

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_download, rootKey)

        findPreference<Preference>(PREFERENCE_INSTALLER_ID)?.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(R.id.installerFragment)
                true
            }
        }

        findPreference<Preference>(INSTALLATION_ABANDON_SESSION)?.apply {
            setOnPreferenceClickListener {
                CommonUtil.cleanupInstallationSessions(requireContext())
                runOnUiThread {
                    requireContext().toast(R.string.toast_abandon_sessions)
                }
                false
            }
        }

        findPreference<Preference>(PREFERENCE_INSTALLATION_DEVICE_OWNER)?.apply {
            val packageName = context.packageName
            val devicePolicyManager =
                context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

            isVisible = devicePolicyManager.isDeviceOwnerApp(packageName)
            setOnPreferenceClickListener {
                context.showDialog(
                    context.getString(R.string.pref_clear_device_owner_title),
                    context.getString(R.string.pref_clear_device_owner_desc),
                    { _: DialogInterface, _: Int ->
                        @Suppress("DEPRECATION")
                        devicePolicyManager.clearDeviceOwnerApp(packageName)
                        activity?.recreate()
                    },
                    { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                )
                true
            }
        }

        findPreference<AuroraListPreference>(PREFERENCE_UPDATES_AUTO)?.setOnPreferenceChangeListener { _, newValue ->
                val value = newValue.toString().toInt()
                when (value) {
                    0 -> {
                        UpdateWorker.cancelAutomatedCheck(requireContext())
                        updatesIntervalPreference?.let {
                            it.isEnabled = false
                        }
                    }

                    1 -> {
                        UpdateWorker.scheduleAutomatedCheck(requireContext())
                        updatesIntervalPreference?.let {
                            it.isEnabled = true
                        }
                    }
                    else -> {
                        if (requireContext().isIgnoringBatteryOptimizations()) {
                            UpdateWorker.scheduleAutomatedCheck(requireContext())
                            updatesIntervalPreference?.let {
                                it.isEnabled = true
                            }
                            return@setOnPreferenceChangeListener true
                        } else {
                            findNavController().navigate(
                                MobileNavigationDirections.actionGlobalDozeWarningSheet(true)
                            )
                        }
                    }
                }
                value != 2
            }

        findPreference<SeekBarPreference>(PREFERENCE_UPDATES_CHECK_INTERVAL)
            ?.setOnPreferenceChangeListener { _, _ ->
                UpdateWorker.updateAutomatedCheck(requireContext())
                true
            }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Toolbar>(R.id.toolbar)?.apply {
            title = getString(R.string.pref_app_download)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        downloadExternalPreference = findPreference(Preferences.PREFERENCE_DOWNLOAD_EXTERNAL)
        downloadDirectoryPreference = findPreference(Preferences.PREFERENCE_DOWNLOAD_DIRECTORY)
        updatesIntervalPreference = findPreference(PREFERENCE_UPDATES_CHECK_INTERVAL)


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

                    true
                }
        }

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
    }
}
