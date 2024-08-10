package com.aurora.store.view.ui.preferences

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.aurora.store.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FiltersDialog: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_menu_about)
            .setTitle(R.string.pref_filters_tip_dialogTitle)
            .setMessage(R.string.pref_filters_tip_dialogSummary)
            .setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                dialog?.dismiss()
            }
            .create()
    }
}
