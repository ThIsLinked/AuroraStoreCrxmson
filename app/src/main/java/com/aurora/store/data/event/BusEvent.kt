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

package com.aurora.store.data.event

sealed class BusEvent {
    data class InstallEvent(
        var packageName: String,
        var extra: String? = ""
    ) : BusEvent()

    data class UninstallEvent(
        var packageName: String,
        var extra: String? = ""
    ) : BusEvent()

    data class Blacklisted(
        var packageName: String,
        var error: String? = ""
    ) : BusEvent()

    data class GoogleAAS(
        var success: Boolean,
        var email: String = String(),
        var aasToken: String = String()
    ) : BusEvent()

    data class ManualDownload(
        var packageName: String,
        var versionCode: Int
    ) : BusEvent()
}

sealed class InstallerEvent {
    data class Success(
        var packageName: String? = "",
        var extra: String? = ""
    ) : InstallerEvent()

    data class Cancelled(
        var packageName: String? = "",
        var extra: String? = ""
    ) : InstallerEvent()

    data class Failed(
        var packageName: String? = "",
        var error: String? = "",
        var extra: String? = ""
    ) : InstallerEvent()
}