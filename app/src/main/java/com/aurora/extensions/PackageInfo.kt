package com.aurora.extensions

import android.content.pm.PackageInfo
import android.os.Process

fun PackageInfo.isApp(): Boolean {
    if (this.applicationInfo == null || this.packageName.isEmpty()) return false

    return if (isQAndAbove()) {
        Process.isApplicationUid(this.applicationInfo!!.uid) &&
                !this.applicationInfo!!.isResourceOverlay && !this.isApex
    } else {
        Process.isApplicationUid(this.applicationInfo!!.uid)
    }
}
