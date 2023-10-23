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

package com.aurora.store.viewmodel.auth

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aurora.Constants
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.PlayResponse
import com.aurora.gplayapi.data.providers.DeviceInfoProvider
import com.aurora.gplayapi.helpers.AuthHelper
import com.aurora.gplayapi.helpers.AuthValidator
import com.aurora.store.AccountType
import com.aurora.store.R
import com.aurora.store.data.AuthState
import com.aurora.store.data.RequestState
import com.aurora.store.data.event.BusEvent
import com.aurora.store.data.model.InsecureAuth
import com.aurora.store.data.network.HttpClient
import com.aurora.store.data.providers.AccountProvider
import com.aurora.store.data.providers.NativeDeviceInfoProvider
import com.aurora.store.data.providers.SpoofProvider
import com.aurora.store.util.AC2DMTask
import com.aurora.store.util.Preferences
import com.aurora.store.util.Preferences.PREFERENCE_AUTH_DATA
import com.aurora.store.viewmodel.BaseAndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.greenrobot.eventbus.EventBus
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.*

class AuthViewModel(application: Application) : BaseAndroidViewModel(application) {

    private val TAG = AuthViewModel::class.java.simpleName

    private val spoofProvider = SpoofProvider(getApplication())

    val liveData: MutableLiveData<AuthState> = MutableLiveData()

    init {
        requestState = RequestState.Init
    }

    override fun observe() {
        if (liveData.value != AuthState.Fetching) {
            val signedIn = Preferences.getBoolean(getApplication(), Constants.ACCOUNT_SIGNED_IN)
            if (signedIn) {
                liveData.postValue(AuthState.Available)
                buildSavedAuthData()
            } else {
                liveData.postValue(AuthState.Unavailable)
            }
        }
    }

    fun buildGoogleAuthData(email: String, aasToken: String) {
        liveData.postValue(AuthState.Fetching)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var properties = NativeDeviceInfoProvider(getApplication()).getNativeDeviceProperties()
                if (spoofProvider.isDeviceSpoofEnabled())
                    properties = spoofProvider.getSpoofDeviceProperties()

                val authData = AuthHelper.build(email, aasToken, properties)
                verifyAndSaveAuth(authData, AccountType.GOOGLE)
            } catch (exception: Exception) {
                liveData.postValue(
                    AuthState.Failed(
                        (getApplication() as Context).getString(R.string.failed_to_generate_session)
                    )
                )
                Log.e(TAG, "Failed to generate Session", exception)
            }
        }
    }

    fun buildAnonymousAuthData() {
        val insecure = Preferences.getBoolean(
            getApplication(),
            Preferences.PREFERENCE_INSECURE_ANONYMOUS
        )

        if (insecure) {
            buildInSecureAnonymousAuthData()
        } else {
            buildSecureAnonymousAuthData()
        }
    }

    private fun buildSecureAnonymousAuthData() {
        liveData.postValue(AuthState.Fetching)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var properties = NativeDeviceInfoProvider(getApplication())
                    .getNativeDeviceProperties()

                if (spoofProvider.isDeviceSpoofEnabled())
                    properties = spoofProvider.getSpoofDeviceProperties()

                val playResponse = HttpClient
                    .getPreferredClient()
                    .postAuth(
                        Constants.URL_DISPENSER,
                        gson.toJson(properties).toByteArray()
                    )

                if (playResponse.isSuccessful) {
                    val authData = gson.fromJson(
                        String(playResponse.responseBytes),
                        AuthData::class.java
                    )

                    //Set AuthData as anonymous
                    authData.isAnonymous = true
                    verifyAndSaveAuth(authData, AccountType.ANONYMOUS)
                } else {
                    throwError(playResponse, getApplication())
                }
            } catch (exception: Exception) {
                liveData.postValue(AuthState.Failed(exception.message.toString()))
                Log.e(TAG, "Failed to generate Session", exception)
            }
        }
    }

    fun buildInSecureAnonymousAuthData() {
        liveData.postValue(AuthState.Fetching)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                var properties = NativeDeviceInfoProvider(getApplication())
                    .getNativeDeviceProperties()

                if (spoofProvider.isDeviceSpoofEnabled())
                    properties = spoofProvider.getSpoofDeviceProperties()

                val playResponse = HttpClient
                    .getPreferredClient()
                    .getAuth(
                        Constants.URL_DISPENSER
                    )

                if (playResponse.isSuccessful) {
                    val insecureAuth = gson.fromJson(
                        String(playResponse.responseBytes),
                        InsecureAuth::class.java
                    )

                    val deviceInfoProvider =
                        DeviceInfoProvider(properties, Locale.getDefault().toString())
                    val authData = AuthHelper.buildInsecure(
                        insecureAuth.email,
                        insecureAuth.auth,
                        Locale.getDefault(),
                        deviceInfoProvider
                    )

                    //Set AuthData as anonymous
                    authData.isAnonymous = true
                    verifyAndSaveAuth(authData, AccountType.ANONYMOUS)
                } else {
                    throwError(playResponse, getApplication())
                }
            } catch (exception: Exception) {
                liveData.postValue(AuthState.Failed(exception.message.toString()))
                Log.e(TAG, "Failed to generate Session", exception)
            }
        }
    }

    fun buildAuthData(context: Context, email: String, oauthToken: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = AC2DMTask().getAC2DMResponse(email, oauthToken)
                if (response.isNotEmpty()) {
                    val aasToken = response["Token"]
                    if (aasToken != null) {
                        Preferences.putString(context, Constants.ACCOUNT_EMAIL_PLAIN, email)
                        Preferences.putString(context, Constants.ACCOUNT_AAS_PLAIN, aasToken)
                        EventBus.getDefault().post(BusEvent.GoogleAAS(true, email, aasToken))
                    } else {
                        Preferences.putString(context, Constants.ACCOUNT_EMAIL_PLAIN, "")
                        Preferences.putString(context, Constants.ACCOUNT_AAS_PLAIN, "")
                        EventBus.getDefault().post(BusEvent.GoogleAAS(false))
                    }
                } else {
                    EventBus.getDefault().post(BusEvent.GoogleAAS(false))
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to build AuthData", exception)
                EventBus.getDefault().post(BusEvent.GoogleAAS(false))
            }
        }
    }

    private fun buildSavedAuthData() {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                try {
                    //Load & validate saved AuthData
                    val savedAuthData = getSavedAuthData()

                    if (isValid(savedAuthData)) {
                        liveData.postValue(AuthState.Valid)
                        requestState = RequestState.Complete
                    } else {
                        //Generate and validate new auth
                        val type = AccountProvider.with(getApplication()).getAccountType()
                        when (type) {
                            AccountType.GOOGLE -> {
                                val email = Preferences.getString(
                                    getApplication(),
                                    Constants.ACCOUNT_EMAIL_PLAIN
                                )
                                val aasToken = Preferences.getString(
                                    getApplication(),
                                    Constants.ACCOUNT_AAS_PLAIN
                                )
                                buildGoogleAuthData(email, aasToken)
                            }
                            AccountType.ANONYMOUS -> {
                                buildAnonymousAuthData()
                            }
                        }
                    }
                } catch (e: Exception) {
                    val error = when (e) {
                        is UnknownHostException -> {
                            (getApplication() as Context).getString(R.string.title_no_network)
                        }
                        is ConnectException -> {
                            (getApplication() as Context).getString(R.string.server_unreachable)
                        }
                        else -> {
                            (getApplication() as Context).getString(R.string.bad_request)
                        }
                    }
                    liveData.postValue(AuthState.Failed(error))
                    requestState = RequestState.Pending
                }
            }
        }
    }

    private fun getSavedAuthData(): AuthData {
        val rawAuth: String = Preferences.getString(getApplication(), PREFERENCE_AUTH_DATA)
        return if (rawAuth.isNotBlank())
            gson.fromJson(rawAuth, AuthData::class.java)
        else
            AuthData("", "")
    }

    private fun isValid(authData: AuthData): Boolean {
        return try {
            AuthValidator(authData)
                .using(HttpClient.getPreferredClient())
                .isValid()
        } catch (e: Exception) {
            false
        }
    }

    private fun verifyAndSaveAuth(authData: AuthData, type: AccountType) {
        liveData.postValue(AuthState.Verifying)

        if (spoofProvider.isLocaleSpoofEnabled()) {
            authData.locale = spoofProvider.getSpoofLocale()
        } else {
            authData.locale = Locale.getDefault()
        }

        if (authData.authToken.isNotEmpty() && authData.deviceConfigToken.isNotEmpty()) {
            configAuthPref(authData, type, true)
            liveData.postValue(AuthState.SignedIn)
            requestState = RequestState.Complete
        } else {
            configAuthPref(authData, type, false)
            liveData.postValue(AuthState.SignedOut)
            requestState = RequestState.Pending

            liveData.postValue(
                AuthState.Failed(
                    (getApplication() as Context).getString(R.string.failed_to_generate_session)
                )
            )
        }
    }

    private fun configAuthPref(authData: AuthData, type: AccountType, signedIn: Boolean) {
        if (signedIn) {
            //Save Auth Data
            Preferences.putString(
                getApplication(),
                PREFERENCE_AUTH_DATA,
                gson.toJson(authData)
            )
        }

        //Save Auth Type
        Preferences.putString(
            getApplication(),
            Constants.ACCOUNT_TYPE,
            type.name  // ANONYMOUS OR GOOGLE
        )

        //Save Auth Status
        Preferences.putBoolean(getApplication(), Constants.ACCOUNT_SIGNED_IN, signedIn)
    }

    @Throws(Exception::class)
    private fun throwError(playResponse: PlayResponse, context: Context) {
        when (playResponse.code) {
            400 -> throw Exception(context.getString(R.string.bad_request))
            403 -> throw Exception(context.getString(R.string.access_denied_using_vpn))
            404 -> throw Exception(context.getString(R.string.server_unreachable))
            429 -> throw Exception(context.getString(R.string.login_rate_limited))
            503 -> throw Exception(context.getString(R.string.server_maintenance))
            else -> {
                if (playResponse.errorString.isNotBlank()) {
                    throw Exception(playResponse.errorString)
                } else {
                    throw Exception(
                        context.getString(
                            R.string.failed_generating_session,
                            playResponse.code
                        )
                    )
                }
            }
        }
    }
}
