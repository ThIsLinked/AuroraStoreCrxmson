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
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aurora.store.R
import com.aurora.store.data.providers.AuthProvider
import com.aurora.store.databinding.FragmentGenericWithPagerBinding
import com.aurora.store.view.ui.commons.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppsGamesFragment : BaseFragment<FragmentGenericWithPagerBinding>() {
    @Inject
    lateinit var authProvider: AuthProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Toolbar>(R.id.toolbar)?.apply {
            title = getString(R.string.title_apps_games)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        // ViewPager
        binding.pager.apply {
            isUserInputEnabled = false
            adapter = ViewPagerAdapter(childFragmentManager, lifecycle)
        }

        TabLayoutMediator(
            binding.tabLayout,
            binding.pager,
            true
        ) { tab: TabLayout.Tab, position: Int ->
            when (position) {
                0 -> tab.text = getString(R.string.title_installed)
                else -> {}
            }
        }.attach()
    }

    internal class ViewPagerAdapter(
        fragment: FragmentManager,
        lifecycle: Lifecycle
    ) :
        FragmentStateAdapter(fragment, lifecycle) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> InstalledAppsFragment.newInstance()
                else -> Fragment()
            }
        }

        override fun getItemCount(): Int {
            return 1
        }
    }
}
