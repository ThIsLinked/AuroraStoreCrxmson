/*
 * Aurora Store
 *  Copyright (C) 2021, Rahul Kumar Patel <whyorean@gmail.com>
 *  Copyright (C) 2022, The Calyx Institute
 *  Copyright (C) 2023, grrfe <grrfe@420blaze.it>
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

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("org.jlleitschuh.gradle.ktlint")
    id("dev.rikka.tools.refine")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.aurora.store"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aurora.store"
        minSdk = 23
        targetSdk = 33

        versionCode = 2311041
        versionName = "4.3.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    buildFeatures {
        viewBinding = true
        aidl = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        lintConfig = file("lint.xml")
    }

    packaging {
        jniLibs.useLegacyPackaging = true
    }

    ndkVersion = "26.1.10909125"
    buildToolsVersion = "34.0.0"
}

dependencies {

    //Protobuf
    implementation("com.google.protobuf:protobuf-javalite:3.24.2")

    //Google's Goodies
    implementation("com.google.android.material:material:1.10.0")
    implementation("com.google.code.gson:gson:2.10.1")

    //AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //Arch LifeCycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-service:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.2")

    //Arch Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.5")

    //Coil
    implementation("io.coil-kt:coil:2.5.0")

    //Shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    //Epoxy
    implementation("com.airbnb.android:epoxy:5.1.3")
    ksp("com.airbnb.android:epoxy-processor:5.1.3")

    //HTTP Clients
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //Fetch - Downloader
    implementation("androidx.tonyodev.fetch2:xfetch2:3.1.6")

    //EventBus
    implementation("org.greenrobot:eventbus:3.3.1")

    //Lib-SU
    implementation("com.github.topjohnwu.libsu:core:5.0.5")

    //Google Play Store API
    implementation("com.gitlab.AuroraOSS:gplayapi:3.2.5")

    //Browser
    implementation("androidx.browser:browser:1.6.0")

    //Shizuku
    compileOnly("dev.rikka.hidden:stub:4.3.1")
    implementation("dev.rikka.tools.refine:runtime:4.4.0")
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")

    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")

    //Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
