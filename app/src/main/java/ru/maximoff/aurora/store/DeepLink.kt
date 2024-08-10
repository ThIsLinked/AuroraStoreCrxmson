/*
    This code snippet was originally created in Java by Maximoff and converted by me and Android Studio into Kotlin. Reserved copyright by Maximoff from plagiarism.
*/
package ru.maximoff.aurora.store

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

open class DeepLink : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        val format: String = if ((uri ?: return).path == "/store/apps/dev") {
            String.format(
                "https://play.google.com/store/apps/dev?id=%s",
                uri.getQueryParameter("id")
            )
        } else {
            String.format(
                "https://play.google.com/store/apps/details?id=%s",
                uri.getQueryParameter("id")
            )
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setClassName(
            "com.aurora.store",
            "com.aurora.store.MainActivity"
        )
        intent.data = Uri.parse(format)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

}
