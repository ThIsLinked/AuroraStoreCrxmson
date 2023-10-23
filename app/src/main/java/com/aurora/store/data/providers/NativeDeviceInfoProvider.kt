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
package com.aurora.store.data.providers

import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.text.TextUtils
import com.aurora.extensions.isHuawei
import java.util.Properties

class NativeDeviceInfoProvider(context: Context) : ContextWrapper(context) {

    fun getNativeDeviceProperties(): Properties {
        val properties = Properties().apply {
            //Build Props
            setProperty("UserReadableName", "${Build.DEVICE}-default")
            setProperty("Build.HARDWARE", Build.HARDWARE)
            setProperty(
                "Build.RADIO",
                if (Build.getRadioVersion() != null)
                    Build.getRadioVersion()
                else
                    "unknown"
            )
            setProperty("Build.FINGERPRINT", Build.FINGERPRINT)
            setProperty("Build.BRAND", Build.BRAND)
            setProperty("Build.DEVICE", Build.DEVICE)
            setProperty("Build.VERSION.SDK_INT", "${Build.VERSION.SDK_INT}")
            setProperty("Build.VERSION.RELEASE", Build.VERSION.RELEASE)
            setProperty("Build.MODEL", Build.MODEL)
            setProperty("Build.MANUFACTURER", Build.MANUFACTURER)
            setProperty("Build.PRODUCT", Build.PRODUCT)
            setProperty("Build.ID", Build.ID)
            setProperty("Build.BOOTLOADER", Build.BOOTLOADER)

            val config = applicationContext.resources.configuration
            setProperty("TouchScreen", "${config.touchscreen}")
            setProperty("Keyboard", "${config.keyboard}")
            setProperty("Navigation", "${config.navigation}")
            setProperty("ScreenLayout", "${config.screenLayout and 15}")
            setProperty("HasHardKeyboard", "${config.keyboard == Configuration.KEYBOARD_QWERTY}")
            setProperty(
                "HasFiveWayNavigation",
                "${config.navigation == Configuration.NAVIGATIONHIDDEN_YES}"
            )

            //Display Metrics
            val metrics = applicationContext.resources.displayMetrics
            setProperty("Screen.Density", "${metrics.densityDpi}")
            setProperty("Screen.Width", "${metrics.widthPixels}")
            setProperty("Screen.Height", "${metrics.heightPixels}")


            //Supported Platforms
            setProperty("Platforms", Build.SUPPORTED_ABIS.joinToString(separator = ","))
            //Supported Features
            setProperty("Features", getFeatures().joinToString(separator = ","))
            //Shared Locales
            setProperty("Locales", getLocales().joinToString(separator = ","))
            //Shared Libraries
            setProperty("SharedLibraries", getSharedLibraries().joinToString(separator = ","))
            //GL Extensions
            val activityManager =
                applicationContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            setProperty(
                "GL.Version",
                activityManager.deviceConfigurationInfo.reqGlEsVersion.toString()
            )
            setProperty(
                "GL.Extensions",
                EglExtensionProvider.eglExtensions.joinToString(separator = ",")
            )

            //Google Related Props
            setProperty("Client", "android-google")
            setProperty("GSF.version", "203615037")
            setProperty("Vending.version", "82201710")
            setProperty("Vending.versionString", "22.0.17-21 [0] [PR] 332555730")

            //MISC
            setProperty("Roaming", "mobile-notroaming")
            setProperty("TimeZone", "UTC-10")

            //Telephony (USA 3650 AT&T)
            setProperty("CellOperator", "310")
            setProperty("SimOperator", "38")
        }

        if (isHuawei())
            stripHuaweiProperties(properties)

        return properties
    }

    private fun getFeatures(): List<String> {
        val featureStringList: MutableList<String> = ArrayList()
        try {
            val availableFeatures = applicationContext.packageManager.systemAvailableFeatures
            for (feature in availableFeatures) {
                if (feature.name.isNotEmpty()) {
                    featureStringList.add(feature.name)
                }
            }
        } catch (e: Exception) {

        }
        return featureStringList
    }

    private fun getLocales(): List<String> {
        val localeList: MutableList<String> = ArrayList()
        localeList.addAll(listOf(*applicationContext.assets.locales))
        val locales: MutableList<String> = ArrayList()
        for (locale in localeList) {
            if (TextUtils.isEmpty(locale)) {
                continue
            }
            locales.add(locale.replace("-", "_"))
        }
        return locales
    }

    private fun getSharedLibraries(): List<String> {
        val systemSharedLibraryNames = applicationContext.packageManager.systemSharedLibraryNames
        val libraries: MutableList<String> = ArrayList()
        try {
            if (systemSharedLibraryNames != null) {
                libraries.addAll(listOf(*systemSharedLibraryNames))
            }
        } catch (e: Exception) {

        }
        return libraries
    }

    private fun stripHuaweiProperties(properties: Properties): Properties {
        //Add Pixel 7a properties
        properties["Build.HARDWARE"] = "lynx"
        properties["Build.BOOTLOADER"] = "lynx-1.0-9716681"
        properties["Build.BRAND"] = "google"
        properties["Build.DEVICE"] = "lynx"
        properties["Build.MODEL"] = "Pixel 7a"
        properties["Build.MANUFACTURER"] = "Google"
        properties["Build.PRODUCT"] = "lynx"
        properties["Build.ID"] = "TQ2A.230505.002"
        return properties
    }
}
