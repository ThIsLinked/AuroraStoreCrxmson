package com.aurora.store.view.ui.preferences

import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.aurora.store.view.custom.preference.M3EditTextPreference
import com.aurora.store.view.custom.preference.M3ListPreference

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {

    override fun onDisplayPreferenceDialog(preference: Preference) {
        when (preference) {
            is EditTextPreference -> {
                val dialogFragment = M3EditTextPreference.newInstance(preference.getKey())
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(
                    parentFragmentManager,
                    M3EditTextPreference.PREFERENCE_DIALOG_FRAGMENT_TAG
                )
            }

            is ListPreference -> {
                val dialogFragment = M3ListPreference.newInstance(preference.getKey())
                dialogFragment.setTargetFragment(this, 0)
                dialogFragment.show(
                    parentFragmentManager,
                    M3ListPreference.PREFERENCE_DIALOG_FRAGMENT_TAG
                )
            }

            else -> super.onDisplayPreferenceDialog(preference)
        }
    }
}
