/*
    This code snippet was originally created in Java by Maximoff and converted by me and Android Studio into Kotlin. Reserved copyright by Maximoff from plagiarism.
*/
package ru.maximoff.aurora.store

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.preference.PreferenceManager
import java.util.Locale

fun loadLanguage(context: Context) {
    val listNames = arrayOf(
        "systemValue",
        "en",
        "ru",
        "uk"
    )
    val locale = when (
        val language = listNames[PreferenceManager.getDefaultSharedPreferences(context)
            .getInt("PREFERENCE_LANGUAGE", 0)]) {
        "systemValue" -> {
            Resources.getSystem().configuration.locales.get(0)
        }

        "en" -> {
            Locale.ROOT
        }

        else -> {
            Locale(language)
        }
    }
    Locale.setDefault(locale)
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(locale)
    configuration.setLayoutDirection(locale)

    context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

}
