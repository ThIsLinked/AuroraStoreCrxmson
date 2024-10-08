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

package com.aurora.store.view.ui.about

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aurora.extensions.browse
import com.aurora.extensions.copyToClipBoard
import com.aurora.store.R
import com.aurora.store.data.model.Link
import com.aurora.store.databinding.FragmentAboutBinding
import com.aurora.store.view.epoxy.views.preference.LinkViewModel_
import com.aurora.store.view.ui.commons.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutFragment : BaseFragment<FragmentAboutBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        binding.epoxyRecycler.layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)

        updateController()
    }

    @SuppressLint("Recycle")
    private fun updateController() {
        val linkURLS = resources.getStringArray(R.array.link_urls)
        val linkTitles = resources.getStringArray(R.array.link_titles)
        val linkSummary = resources.getStringArray(R.array.link_subtitle)
        val linkIcons = resources.obtainTypedArray(R.array.link_icons)

        binding.epoxyRecycler.withModels {
            for (i in linkURLS.indices) {
                val link = Link(
                    id = i,
                    title = linkTitles[i],
                    subtitle = linkSummary[i],
                    url = linkURLS[i],
                    icon = linkIcons.getResourceId(i, 0)
                )
                add(LinkViewModel_()
                    .id(i)
                    .link(link)
                    /*.click { _ ->
                        if (link.id == 0) {
                            findNavController().navigate(R.id.aboutDialog)
                        } else {
                            processUrl(link.url)
                        }
                    }*/
                    .click { _ -> processUrl(link.url)})
            }
        }
    }

    private fun processUrl(url: String) {
        when {
            url.startsWith("http") -> context?.browse(url)
            url.startsWith("upi") -> context?.browse(url)
            else -> context?.copyToClipBoard(url)
        }
    }
}
