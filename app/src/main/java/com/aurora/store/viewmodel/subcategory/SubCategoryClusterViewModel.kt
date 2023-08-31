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

package com.aurora.store.viewmodel.subcategory

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.CategoryHelper
import com.aurora.store.data.RequestState
import com.aurora.store.data.ViewState
import com.aurora.store.data.network.HttpClient
import com.aurora.store.data.providers.AuthProvider
import com.aurora.store.util.Log
import com.aurora.store.viewmodel.BaseAndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class SubCategoryClusterViewModel(application: Application) : BaseAndroidViewModel(application) {

    var authData: AuthData = AuthProvider.with(application).getAuthData()
    var categoryHelper: CategoryHelper = CategoryHelper(authData)
        .using(HttpClient.getPreferredClient())

    val liveData: MutableLiveData<ViewState> = MutableLiveData()
    var streamBundle: StreamBundle = StreamBundle()

    lateinit var homeUrl: String

    init {
        liveData.postValue(ViewState.Loading)
    }

    private fun getCategoryStreamBundle(
        nextPageUrl: String
    ): StreamBundle {
        return if (streamBundle.streamClusters.isEmpty())
            categoryHelper.getSubCategoryBundle(homeUrl)
        else
            categoryHelper.getSubCategoryBundle(nextPageUrl)
    }

    fun observeCategory(homeUrl: String) {
        this.homeUrl = homeUrl
        observe()
    }

    override fun observe() {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    if (!streamBundle.hasCluster() || streamBundle.hasNext()) {
                        //Fetch new stream bundle
                        val newBundle = getCategoryStreamBundle(
                            streamBundle.streamNextPageUrl
                        )

                        //Update old bundle
                        streamBundle.apply {
                            streamClusters.putAll(newBundle.streamClusters)
                            streamNextPageUrl = newBundle.streamNextPageUrl
                        }

                        //Post updated to UI
                        liveData.postValue(ViewState.Success(streamBundle))
                    } else {
                        Log.i("End of Bundle")
                        requestState = RequestState.Complete
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    requestState = RequestState.Pending
                    liveData.postValue(ViewState.Error(e.message))
                }
            }
        }
    }

    fun observeCluster(streamCluster: StreamCluster) {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    if (streamCluster.hasNext()) {
                        val newCluster =
                            categoryHelper.getNextStreamCluster(streamCluster.clusterNextPageUrl)
                        updateCluster(newCluster)
                        liveData.postValue(ViewState.Success(streamBundle))
                    } else {
                        Log.i("End of cluster")
                        streamCluster.clusterNextPageUrl = String()
                    }
                } catch (e: Exception) {
                    liveData.postValue(ViewState.Error(e.message))
                }
            }
        }
    }

    private fun updateCluster(newCluster: StreamCluster) {
        streamBundle.streamClusters[newCluster.id]?.apply {
            clusterAppList.addAll(newCluster.clusterAppList)
            clusterNextPageUrl = newCluster.clusterNextPageUrl
        }
    }
}
