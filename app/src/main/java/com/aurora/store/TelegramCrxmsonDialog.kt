package com.aurora.store

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.aurora.Constants
import com.aurora.store.util.Preferences
import com.aurora.store.util.save
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TelegramCrxmsonDialog : DialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setIcon(R.drawable.ic_telegram)
            .setTitle(R.string.telegramCrxmsonDialog_title)
            .setMessage(R.string.telegramCrxmsonDialog_summary)
            .setPositiveButton(getString(R.string.telegramCrxmsonDialog_button)) { _, _ ->
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(Constants.TELEGRAM_CRXMSON_LINK)
                    )
                )
                dialog?.dismiss()
            }
            .create()
    }

}
