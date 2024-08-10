package com.aurora.store

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.aurora.store.util.Log
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

@AndroidEntryPoint
class TerminatedWorkingAurora : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_menu_exit)
            .setTitle(R.string.terminatedworkingaurora_dialogTitle)
            .setMessage(R.string.terminatedworkingaurora_dialogSummary)
            .setPositiveButton(getString(R.string.terminatedworkingaurora_dialogPositiveButton)) { _, _ ->
                Log.terminatedNotification()
                activity?.finishAffinity()
                exitProcess(0)
            }
            .setNegativeButton(getString(R.string.terminatedworkingaurora_dialogNegativeButton)) { _, _ ->
                dialog?.dismiss()
            }
            .create()
    }

}
