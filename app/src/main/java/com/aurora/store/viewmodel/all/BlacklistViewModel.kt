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

package com.aurora.store.viewmodel.all

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.extensions.isApp
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.store.data.network.IProxyHttpClient
import com.aurora.store.data.providers.AuthProvider
import com.aurora.store.data.providers.BlacklistProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak") // false positive, see https://github.com/google/dagger/issues/3253
class BlacklistViewModel @Inject constructor(
    val blacklistProvider: BlacklistProvider,
    @ApplicationContext private val context: Context,
    httpClient: IProxyHttpClient,
    authProvider: AuthProvider
) : ViewModel() {
    private val appDetailsHelper = AppDetailsHelper(authProvider.authData!!)
        .using(httpClient)

    private val _blacklistedApps = MutableStateFlow<List<App>?>(null)
    val blackListedApps: StateFlow<List<App>?> = _blacklistedApps.asStateFlow()

    var selected: MutableSet<String> = mutableSetOf()

    init {
        selected = blacklistProvider.blacklist
        fetchApps()
    }

    private fun fetchApps() {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    val packageNames = context.packageManager.getInstalledPackages(0)
                        .filter { it.isApp() }
                        .map { it.packageName }

                    val apps = appDetailsHelper
                        .getAppByPackageName(packageNames)
                        .filter { it.displayName.isNotBlank() }

                    _blacklistedApps.emit(apps.sortedBy { it.displayName.lowercase(Locale.getDefault()) })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
