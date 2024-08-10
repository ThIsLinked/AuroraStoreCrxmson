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

package com.aurora.store.view.ui.sheets

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import coil.load
import coil.transform.CircleCropTransformation
import com.aurora.extensions.toast
import com.aurora.store.R
import com.aurora.store.databinding.SheetDeviceMiuiBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeviceMiuiSheet : BaseDialogSheet<SheetDeviceMiuiBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPrimary.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            } catch (e: Exception) {
                toast(R.string.toast_developer_setting_failed)
            }
        }

        binding.btnSecondary.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}
