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

package com.aurora.store.viewmodel.sale

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AppSalesHelper
import com.aurora.store.data.RequestState
import com.aurora.store.data.network.HttpClient
import com.aurora.store.data.providers.AuthProvider
import com.aurora.store.viewmodel.BaseAndroidViewModel
import com.aurora.store.viewmodel.all.PaginatedAppList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class AppSalesViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val authData: AuthData = AuthProvider.with(application).getAuthData()
    private val appSalesHelper: AppSalesHelper =
        AppSalesHelper(authData).using(HttpClient.getPreferredClient(application))

    private var page: Int = 0
    private val appList: MutableList<App> = mutableListOf()

    val liveData: MutableLiveData<PaginatedAppList> = MutableLiveData()

    init {
        requestState = RequestState.Init
        observe()
    }

    override fun observe() {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                requestState = try {
                    val nextAppList = getSearchResults()

                    if (nextAppList.isEmpty()) {
                        liveData.postValue(
                            PaginatedAppList(
                                appList,
                                hasMore = false
                            )
                        )
                    } else {
                        appList.addAll(nextAppList)
                        liveData.postValue(
                            PaginatedAppList(
                                appList,
                                hasMore = true
                            )
                        )
                    }

                    RequestState.Complete
                } catch (e: Exception) {
                    RequestState.Pending
                }
            }
        }
    }

    private fun getSearchResults(
    ): List<App> {
        return try {
            appSalesHelper.getAppsOnSale(page = page++, offer = 100)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
