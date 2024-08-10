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

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.aurora.store.util.Preferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlacklistProvider @Inject constructor(
    private val gson: Gson,
    @ApplicationContext val context: Context,
) {

    private val preferenceBlacklist = "PREFERENCE_BLACKLIST"

    var blacklist: MutableSet<String>
        set(value) = Preferences.putString(context, preferenceBlacklist, gson.toJson(value))
        get() {
            return try {
                val refMethod = Context::class.java.getDeclaredMethod(
                    "getSharedPreferences",
                    File::class.java,
                    Int::class.java
                )
                val refSharedPreferences = refMethod.invoke(
                    context,
                    File("/product/etc/com.aurora.store/blacklist.xml"),
                    Context.MODE_PRIVATE
                ) as SharedPreferences

                val rawBlacklist = PreferenceManager.getDefaultSharedPreferences(context).getString(
                    preferenceBlacklist,
                    refSharedPreferences.getString(preferenceBlacklist, "")
                )

                if (rawBlacklist!!.isEmpty())
                    mutableSetOf()
                else
                    gson.fromJson(rawBlacklist, object : TypeToken<Set<String?>?>() {}.type)
            } catch (e: Exception) {
                mutableSetOf()
            }
        }

    fun isBlacklisted(packageName: String): Boolean {
        return blacklist.contains(packageName)
    }


    fun blacklist(packageName: String) {
        blacklist = blacklist.apply {
            add(packageName)
        }
    }

    fun whitelist(packageName: String) {
        blacklist = blacklist.apply {
            remove(packageName)
        }
    }

}
