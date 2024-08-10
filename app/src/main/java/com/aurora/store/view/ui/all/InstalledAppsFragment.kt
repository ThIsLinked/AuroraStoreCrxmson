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

package com.aurora.store.view.ui.all

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.aurora.gplayapi.data.models.App
import com.aurora.store.AuroraApp
import com.aurora.store.R
import com.aurora.store.data.event.InstallerEvent
import com.aurora.store.data.model.MinimalApp
import com.aurora.store.databinding.FragmentAppsBinding
import com.aurora.store.view.epoxy.views.HeaderViewModel_
import com.aurora.store.view.epoxy.views.app.AppListViewModel_
import com.aurora.store.view.epoxy.views.shimmer.AppListViewShimmerModel_
import com.aurora.store.view.ui.commons.BaseFragment
import com.aurora.store.viewmodel.all.InstalledViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InstalledAppsFragment : BaseFragment<FragmentAppsBinding>() {

    private val tag = InstalledAppsFragment::class.java.simpleName
    private val viewModel: InstalledViewModel by viewModels()

    companion object {
        @JvmStatic
        fun newInstance(): InstalledAppsFragment {
            return InstalledAppsFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.installedApps.collect {
                updateController(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            AuroraApp.events.installerEvent.collect {
                when (it) {
                    is InstallerEvent.Installed,
                    is InstallerEvent.Uninstalled -> {
                        viewModel.fetchApps()
                    }

                    else -> {
                        Log.i(tag, "Got an unhandled event: $it")
                    }
                }
            }
        }
    }

    private fun updateController(appList: List<App>?) {
        binding.recycler.withModels {
            setFilterDuplicates(true)
            if (appList == null) {
                for (i in 1..10) {
                    add(
                        AppListViewShimmerModel_()
                            .id(i)
                    )
                }
            } else {
                add(
                    HeaderViewModel_()
                        .id("header")
                        .title(getString(R.string.myapps_installed, appList.size))
                )
                appList.forEach { app ->
                    add(
                        AppListViewModel_()
                            .id(app.id)
                            .app(app)
                            .click { _ -> openDetailsFragment(app.packageName, app) }
                            .longClick { _ ->
                                openAppMenuSheet(MinimalApp.fromApp(app))
                                false
                            }
                    )
                }
            }
        }
    }

}
