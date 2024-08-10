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

package com.aurora.store.view.custom.layouts.button

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import com.aurora.extensions.accentColor
import com.aurora.extensions.darkenColor
import com.aurora.extensions.getString
import com.aurora.extensions.lightenColor
import com.aurora.extensions.runOnUiThread
import com.aurora.store.R
import com.aurora.store.data.model.DownloadStatus
import com.aurora.store.databinding.ViewUpdateButtonBinding

class UpdateButton : LinearLayoutCompat {

    private lateinit var binding: ViewUpdateButtonBinding

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        val view = inflate(context, R.layout.view_update_button, this)
        binding = ViewUpdateButtonBinding.bind(view)

        // Apply primaryColor tint to all buttons with alpha
        val alphaAccent = lightenColor(context.accentColor(), alpha = 200)
        binding.btnPositive.backgroundTintList = ColorStateList.valueOf(alphaAccent)
        binding.btnNegative.backgroundTintList = ColorStateList.valueOf(alphaAccent)

        val textColor = darkenColor(context.accentColor())
        binding.btnPositive.setTextColor(textColor)
        binding.btnNegative.setTextColor(textColor)
    }

    fun setText(text: String) {
        binding.viewFlipper.displayedChild = 0
        binding.btnPositive.text = text
    }

    fun setText(text: Int) {
        binding.viewFlipper.displayedChild = 0
        binding.btnPositive.text = getString(text)
    }

    fun updateState(downloadStatus: DownloadStatus) {
        val displayChild = when (downloadStatus) {
            DownloadStatus.QUEUED -> 1
            DownloadStatus.DOWNLOADING -> 2
            else -> 0
        }

        if (binding.viewFlipper.displayedChild != displayChild) {
            runOnUiThread {
                binding.viewFlipper.displayedChild = displayChild
            }
        }
    }

    fun addPositiveOnClickListener(onClickListener: OnClickListener?) {
        binding.btnPositive.setOnClickListener(onClickListener)
    }

    fun addNegativeOnClickListener(onClickListener: OnClickListener?) {
        binding.btnNegative.setOnClickListener(onClickListener)
    }
}
