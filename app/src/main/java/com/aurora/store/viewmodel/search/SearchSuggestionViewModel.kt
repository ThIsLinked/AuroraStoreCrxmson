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

package com.aurora.store.viewmodel.search

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.helpers.SearchHelper
import com.aurora.gplayapi.helpers.contracts.SearchContract
import com.aurora.gplayapi.helpers.web.WebSearchHelper
import com.aurora.store.data.network.IProxyHttpClient
import com.aurora.store.data.providers.AuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak") // false positive, see https://github.com/google/dagger/issues/3253
class SearchSuggestionViewModel @Inject constructor(
    private val authProvider: AuthProvider,
    httpClient: IProxyHttpClient
) : ViewModel() {

    private val webSearchHelper: WebSearchHelper = WebSearchHelper()
    private val searchHelper: SearchHelper = SearchHelper(authProvider.authData!!)
        .using(httpClient)

    val liveSearchSuggestions: MutableLiveData<List<SearchSuggestEntry>> = MutableLiveData()

    fun helper(): SearchContract {
        return if (authProvider.isAnonymous) {
            webSearchHelper
        } else {
            searchHelper
        }
    }

    fun observeStreamBundles(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            liveSearchSuggestions.postValue(getSearchSuggestions(query))
        }
    }

    private fun getSearchSuggestions(
        query: String
    ): List<SearchSuggestEntry> {
        return helper().searchSuggestions(query)
    }
}
