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

package com.aurora.store.viewmodel.review

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.Review
import com.aurora.gplayapi.data.models.ReviewCluster
import com.aurora.gplayapi.helpers.ReviewsHelper
import com.aurora.store.data.RequestState
import com.aurora.store.data.network.HttpClient
import com.aurora.store.data.providers.AuthProvider
import com.aurora.store.viewmodel.BaseAndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class ReviewViewModel(application: Application) : BaseAndroidViewModel(application) {

    var authData: AuthData = AuthProvider
        .with(application)
        .getAuthData()

    var reviewsHelper: ReviewsHelper = ReviewsHelper(authData)
        .using(HttpClient.getPreferredClient())

    val liveData: MutableLiveData<ReviewCluster> = MutableLiveData()

    private lateinit var reviewsCluster: ReviewCluster

    fun fetchReview(packageName: String, filter: Review.Filter) {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    reviewsCluster = reviewsHelper.getReviews(packageName, filter)
                    liveData.postValue(reviewsCluster)
                } catch (e: Exception) {
                    requestState = RequestState.Pending
                }
            }
        }
    }

    fun next(nextReviewPageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    val nextReviewCluster = reviewsHelper.next(nextReviewPageUrl)

                    reviewsCluster.apply {
                        reviewList.addAll(nextReviewCluster.reviewList)
                        nextPageUrl = nextReviewCluster.nextPageUrl
                    }

                    liveData.postValue(reviewsCluster)
                } catch (e: Exception) {
                    requestState = RequestState.Pending
                }
            }
        }
    }

    override fun observe() {

    }
}