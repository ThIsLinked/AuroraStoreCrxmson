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

package com.aurora.store.view.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.aurora.extensions.isQAndAbove
import com.aurora.extensions.isSAndAbove
import com.aurora.store.R
import com.aurora.store.data.work.UpdateWorker
import com.aurora.store.databinding.FragmentOnboardingBinding
import com.aurora.store.util.PathUtil
import com.aurora.store.util.Preferences
import com.aurora.store.util.Preferences.PREFERENCE_AUTO_DELETE
import com.aurora.store.util.Preferences.PREFERENCE_AUTO_INSTALL
import com.aurora.store.util.Preferences.PREFERENCE_DEFAULT
import com.aurora.store.util.Preferences.PREFERENCE_DEFAULT_SELECTED_TAB
import com.aurora.store.util.Preferences.PREFERENCE_DOWNLOAD_ACTIVE
import com.aurora.store.util.Preferences.PREFERENCE_DOWNLOAD_DIRECTORY
import com.aurora.store.util.Preferences.PREFERENCE_DOWNLOAD_EXTERNAL
import com.aurora.store.util.Preferences.PREFERENCE_DOWNLOAD_WIFI_ONLY
import com.aurora.store.util.Preferences.PREFERENCE_FILTER_AURORA_ONLY
import com.aurora.store.util.Preferences.PREFERENCE_FILTER_FDROID
import com.aurora.store.util.Preferences.PREFERENCE_FILTER_GOOGLE
import com.aurora.store.util.Preferences.PREFERENCE_FILTER_SEARCH
import com.aurora.store.util.Preferences.PREFERENCE_FOR_YOU
import com.aurora.store.util.Preferences.PREFERENCE_INSECURE_ANONYMOUS
import com.aurora.store.util.Preferences.PREFERENCE_INSTALLER_ID
import com.aurora.store.util.Preferences.PREFERENCE_INTRO
import com.aurora.store.util.Preferences.PREFERENCE_LANGUAGE
import com.aurora.store.util.Preferences.PREFERENCE_PROXY_ENABLED
import com.aurora.store.util.Preferences.PREFERENCE_PROXY_INFO
import com.aurora.store.util.Preferences.PREFERENCE_PROXY_URL
import com.aurora.store.util.Preferences.PREFERENCE_SELF_UPDATE
import com.aurora.store.util.Preferences.PREFERENCE_SIMILAR
import com.aurora.store.util.Preferences.PREFERENCE_THEME_ACCENT
import com.aurora.store.util.Preferences.PREFERENCE_THEME_TYPE
import com.aurora.store.util.Preferences.PREFERENCE_UPDATES_CHECK
import com.aurora.store.util.Preferences.PREFERENCE_UPDATES_CHECK_INTERVAL
import com.aurora.store.util.Preferences.PREFERENCE_UPDATES_EXTENDED
import com.aurora.store.util.Preferences.PREFERENCE_VENDING_VERSION
import com.aurora.store.util.save
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private var lastPosition = 0

    internal class PagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
        FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return WelcomeFragment()
                1 -> return ThemeFragment()
                2 -> return InstallerFragment()
                3 -> return AppLinksFragment()
                4 -> return PermissionsFragment()
            }
            return Fragment()
        }

        override fun getItemCount(): Int {
            return 5
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingBinding.bind(view)

        val isDefaultPrefLoaded = Preferences.getBoolean(requireContext(), PREFERENCE_DEFAULT)
        if (!isDefaultPrefLoaded) {
            save(PREFERENCE_DEFAULT, true)
            loadDefaultPreferences()
        }

        // ViewPager2
        binding.viewpager2.apply {
            adapter = PagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
            isUserInputEnabled = false
            setCurrentItem(0, true)
            registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    activity?.runOnUiThread {
                        lastPosition = position
                        refreshButtonState()
                    }
                }
            })
        }

        TabLayoutMediator(binding.tabLayout, binding.viewpager2, true) { tab, position ->
            tab.text = (position + 1).toString()
        }.attach()

        binding.btnForward.setOnClickListener {
            binding.viewpager2.setCurrentItem(binding.viewpager2.currentItem + 1, true)
        }

        binding.btnBackward.setOnClickListener {
            binding.viewpager2.setCurrentItem(binding.viewpager2.currentItem - 1, true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun refreshButtonState() {
        binding.btnBackward.isEnabled = lastPosition != 0
        binding.btnForward.isEnabled = lastPosition != 5

        if (lastPosition == 0) {
            binding.btnBackward.visibility = View.INVISIBLE
        } else {
            binding.btnBackward.visibility = View.VISIBLE
        }

        if (lastPosition == 4) {
            binding.btnForward.text = getString(R.string.action_finish)
            binding.btnForward.isEnabled = true
            binding.btnForward.setOnClickListener {
                save(PREFERENCE_INTRO, true)
                UpdateWorker.scheduleAutomatedCheck(requireContext())
                findNavController().navigate(
                    OnboardingFragmentDirections.actionOnboardingFragmentToSplashFragment()
                )
            }
        } else {
            binding.btnForward.text = getString(R.string.action_next)
            binding.btnForward.setOnClickListener {
                binding.viewpager2.setCurrentItem(
                    binding.viewpager2.currentItem + 1, true
                )
            }
        }
    }

    private fun loadDefaultPreferences() {

        /* General */
        save(PREFERENCE_SELF_UPDATE, true)

        /* Interface */
        save(PREFERENCE_LANGUAGE, 0)
        save(
            PREFERENCE_THEME_TYPE, if (isQAndAbove()) { // Condition: Android Q and above
                0 // The answer is positive – action: Set value is 0 (inherit system theme value)
            } else {
                1 // The answer is negative – action: Set value is 1 (Light theme)
            }
        )
        save(
            PREFERENCE_THEME_ACCENT, if (isSAndAbove()) { // Condition: Android S and above
                0 // The answer is positive – action: Set value is 0 (system dynamic color value)
            } else {
                1 // The answer is negative – action: Set value is 1 (Crxmson color value)
            }
        )
        save(PREFERENCE_DEFAULT_SELECTED_TAB, 0)
        save(PREFERENCE_FOR_YOU, false)
        save(PREFERENCE_SIMILAR, false)
        save(PREFERENCE_FILTER_SEARCH, false)
        save(PREFERENCE_FILTER_AURORA_ONLY, false)
        save(PREFERENCE_FILTER_GOOGLE, false)
        save(PREFERENCE_FILTER_FDROID, false)

        /* Downloads */
        save(PREFERENCE_DOWNLOAD_EXTERNAL, true)
        save(PREFERENCE_DOWNLOAD_DIRECTORY, PathUtil.getExternalPath(requireContext()))
        save(PREFERENCE_DOWNLOAD_WIFI_ONLY, false)
        save(PREFERENCE_DOWNLOAD_ACTIVE, 4)
        save(PREFERENCE_INSTALLER_ID, 0)
        save(PREFERENCE_AUTO_INSTALL, false)
        save(PREFERENCE_AUTO_DELETE, false)
        save(PREFERENCE_UPDATES_EXTENDED, false)
        save(PREFERENCE_UPDATES_CHECK, true)
        save(PREFERENCE_UPDATES_CHECK_INTERVAL, 24)

        /* Network */
        save(PREFERENCE_INSECURE_ANONYMOUS, false)
        save(PREFERENCE_VENDING_VERSION, 0)
        save(PREFERENCE_PROXY_ENABLED, false)
        save(PREFERENCE_PROXY_URL, "")
        save(PREFERENCE_PROXY_INFO, "{}")
    }
}
