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

package com.aurora.store.view.ui.commons

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.StreamBundle
import com.aurora.gplayapi.data.models.StreamCluster
import com.aurora.gplayapi.helpers.contracts.StreamContract
import com.aurora.gplayapi.utils.CategoryUtil
import com.aurora.store.R
import com.aurora.store.data.model.ViewState
import com.aurora.store.data.model.ViewState.Loading.getDataAs
import com.aurora.store.databinding.FragmentGenericWithToolbarBinding
import com.aurora.store.view.custom.recycler.EndlessRecyclerOnScrollListener
import com.aurora.store.view.epoxy.controller.CategoryCarouselController
import com.aurora.store.view.epoxy.controller.GenericCarouselController
import com.aurora.store.viewmodel.subcategory.CategoryStreamViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryBrowseFragment : BaseFragment<FragmentGenericWithToolbarBinding>(),
    GenericCarouselController.Callbacks {
    private val args: CategoryBrowseFragmentArgs by navArgs()
    internal val viewModel: CategoryStreamViewModel by activityViewModels()

    private lateinit var category: StreamContract.Category
    private var streamBundle: StreamBundle? = StreamBundle()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Toolbar>(R.id.toolbar)?.apply {
            title = args.title
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        category = CategoryUtil.getCategoryFromUrl(args.browseUrl)

        val genericCarouselController = CategoryCarouselController(this)

        // RecyclerView
        binding.recycler.setController(genericCarouselController)
        binding.recycler.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onLoadMore(currentPage: Int) {
                viewModel.observe(category)
            }
        })

        viewModel.getStreamBundle(category)
        viewModel.liveData.observe(viewLifecycleOwner) {
            when (it) {
                is ViewState.Loading -> {
                    genericCarouselController.setData(null)
                }

                is ViewState.Success<*> -> {
                    val stash = it.getDataAs<Map<String, StreamBundle>>()
                    streamBundle = stash[category.value]

                    genericCarouselController.setData(streamBundle)
                }

                else -> {}
            }
        }
    }

    override fun onHeaderClicked(streamCluster: StreamCluster) {
        openStreamBrowseFragment(streamCluster)
    }

    override fun onClusterScrolled(streamCluster: StreamCluster) {
        viewModel.observeCluster(category, streamCluster)
    }

    override fun onAppClick(app: App) {
        openDetailsFragment(app.packageName, app)
    }

    override fun onAppLongClick(app: App) {

    }
}
