/*
 * Aurora Store
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Store is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Aurora Store is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
package com.aurora.store.view.ui.sheets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aurora.Constants
import com.aurora.store.BuildConfig
import com.aurora.store.R
import com.aurora.store.data.model.SelfUpdate
import com.aurora.store.databinding.SheetSelfUpdateBinding


class SelfUpdateSheet : BaseBottomSheet() {

    private lateinit var B: SheetSelfUpdateBinding
    private lateinit var selfUpdate: SelfUpdate

    companion object {

        const val TAG = "ManualDownloadSheet"

        @JvmStatic
        fun newInstance(
            selfUpdate: SelfUpdate
        ): SelfUpdateSheet {
            return SelfUpdateSheet().apply {
                arguments = Bundle().apply {
                    putString(Constants.STRING_EXTRA, gson.toJson(selfUpdate))
                }
            }
        }
    }

    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        B = SheetSelfUpdateBinding.inflate(inflater)
        return B.root
    }

    override fun onContentViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        bundle?.let {
            val rawUpdate = bundle.getString(Constants.STRING_EXTRA, "{}")
            selfUpdate = gson.fromJson(rawUpdate, SelfUpdate::class.java)
            if (selfUpdate.versionName.isNotEmpty()) {
                inflateData()
                attachActions()
            } else {
                dismissAllowingStateLoss()
            }
        }
    }

    private fun inflateData() {
        val currentVersionName = BuildConfig.VERSION_NAME // Get current versionName as reference
        val currentVersionCode = BuildConfig.VERSION_CODE // Get current versionCode as reference
        val newVersionName = selfUpdate.versionName // Get new versionName as reference
        val newVersionCode = selfUpdate.versionCode // Get new versionName as reference
        B.txtLine2.text = getString( // Set current and new version
            R.string.sheet_self_update_newVersion, // Get format layout
            currentVersionName, // Get current versionName
            currentVersionCode, // Get current versionCode
            newVersionName, // Get new versionName
            newVersionCode // Get new versionCode
        )

        val changelogText: String = selfUpdate.changelog.ifEmpty { getString(R.string.details_changelog_unavailable) } // Get changelog text as reference
        B.txtChangelog.text = changelogText.trim() // Set changelog text with trim parameter
        B.txtChangelog.movementMethod = ScrollingMovementMethod() // Set scrollability
        B.txtChangelog.isScrollbarFadingEnabled = false
    }

    private fun attachActions() {

        /* Build on 4PDA */
        // Action
        B.btnPrimary.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://4pda.to/forum/index.php?act=findpost&pid=116441910&anchor=Spoil-116441910-6")
                )
            )
        }

        /* Build on GitHub */
        // Action
        B.btnSecondary.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/ThIsLinked/AuroraStoreCrxmson/releases")
                )
            )
        }

        /* Don't update */
        // Action
        B.btnTertiary.setOnClickListener {
            dismissAllowingStateLoss()
        }

    }

}
