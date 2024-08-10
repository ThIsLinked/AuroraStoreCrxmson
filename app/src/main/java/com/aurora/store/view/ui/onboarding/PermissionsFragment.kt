/*
 * Aurora Store
 *  Copyright (C) 2021, Rahul Kumar Patel <whyorean@gmail.com>
 *  Copyright (C) 2022, The Calyx Institute
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

package com.aurora.store.view.ui.onboarding

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.aurora.extensions.isRAndAbove
import com.aurora.extensions.isSAndAbove
import com.aurora.extensions.isTAndAbove
import com.aurora.store.PermissionType
import com.aurora.store.R
import com.aurora.store.data.model.Permission
import com.aurora.store.data.providers.PermissionProvider
import com.aurora.store.databinding.FragmentOnboardingPermissionsBinding
import com.aurora.store.view.epoxy.views.preference.PermissionViewModel_
import com.aurora.store.view.ui.commons.BaseFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionsFragment : BaseFragment<FragmentOnboardingPermissionsBinding>() {
    private lateinit var permissionProvider: PermissionProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionProvider = PermissionProvider(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateController()

        val playStorePackageName = "com.android.vending"

        fun playStoreIsInstalled() : Boolean {
            return try {
                requireContext().packageManager.getPackageInfo(playStorePackageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }

        /* Tip frame */
        // Condition: Play Store is not present among the apps.
        if (playStoreIsInstalled()) {

            // Condition: Links to Play Store domains have been confirmed for Aurora.
            if (permissionProvider.isGranted(permissionType = PermissionType.APP_LINKS)) {
                view.findViewById<MaterialCardView>(R.id.onboarding_permission_appslinks_tip_frame).visibility = View.GONE // Hide the layout.
                Log.i("OnboardingAppsLinks", "All links have been confirmed, the tip was hidden.") // Sending a notification about confirmed links in Logcat.
            } else {
                val tipTitleButton = binding.onboardingPermissionAppslinksTipTitleButton
                val tipDesc = binding.onboardingPermissionAppslinksTipDesc

                tipTitleButton.setOnClickListener {
                    if (!tipDesc.isVisible) {
                        tipTitleButton.icon = context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_arrow_up)
                        }
                        tipDesc.visibility = View.VISIBLE
                    } else {
                        tipTitleButton.icon = context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_arrow_down )
                        }
                        tipDesc.visibility = View.GONE
                    }
                }

                /* Tip link to official documentation */
                view.findViewById<MaterialButton>(R.id.onboarding_permission_appslinks_tip_buttonDocumentation).setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com/training/app-links/verify-android-applinks")))
                }

                /* Tip for opening system settings Play Store */
                view.findViewById<MaterialButton>(R.id.onboarding_permission_appslinks_tip_buttonSettings).setOnClickListener {
                    startActivity(Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", playStorePackageName, null)
                    })
                }
            }

        } else {
            Log.i("OnboardingAppsLinks", "Play Store is not present among the apps, the tip was hidden.") // Sending notification absence a Play Store to Logcat.
        }

    }

    override fun onResume() {
        super.onResume()
        updateController()
    }

    override fun onDestroy() {
        permissionProvider.unregister()
        super.onDestroy()
    }

    private fun permissionList(): List<Permission> {
        val permissions = mutableListOf(
            Permission(
                PermissionType.INSTALL_UNKNOWN_APPS,
                getString(R.string.onboarding_permission_installer),
                getString(R.string.onboarding_permission_installer_desc)
            )
        )

        if (isRAndAbove()) {
            permissions.add(
                Permission(
                    PermissionType.STORAGE_MANAGER,
                    getString(R.string.onboarding_permission_esm),
                    getString(R.string.onboarding_permission_esm_desc)
                )
            )
        } else {
            permissions.add(
                Permission(
                    PermissionType.EXTERNAL_STORAGE,
                    getString(R.string.onboarding_permission_esa),
                    getString(R.string.onboarding_permission_esa_desc)
                )
            )
        }

        permissions.add(
            Permission(
                PermissionType.DOZE_WHITELIST,
                getString(R.string.onboarding_permission_doze),
                getString(R.string.onboarding_permission_doze_desc)
            )
        )

        if (isTAndAbove()) {
            permissions.add(
                Permission(
                    PermissionType.POST_NOTIFICATIONS,
                    getString(R.string.onboarding_permission_notifications),
                    getString(R.string.onboarding_permission_notifications_desc)
                )
            )
        }

        if (isSAndAbove()) {
            permissions.add(
                Permission(
                    PermissionType.APP_LINKS,
                    getString(R.string.onboarding_permission_apps_links_title),
                    getString(R.string.onboarding_permission_apps_links_desc)
                ),
            )
        }

        return permissions

    }

    private fun updateController() {
        binding.epoxyRecycler.withModels {
            setFilterDuplicates(true)

            permissionList().forEach {
                add(renderPermissionView(it))
            }

        }

    }

    private fun renderPermissionView(permission: Permission): PermissionViewModel_ {
        return PermissionViewModel_()
            .id(permission.type.name)
            .permission(permission)
            .isGranted(permissionProvider.isGranted(permission.type))
            .click { _ -> permissionProvider.request(permission.type) }
    }

}
