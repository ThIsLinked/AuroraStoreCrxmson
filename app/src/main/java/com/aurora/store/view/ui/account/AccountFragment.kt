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

package com.aurora.store.view.ui.account

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.aurora.Constants.URL_DISCLAIMER
import com.aurora.Constants.URL_LICENSE
import com.aurora.Constants.URL_TOS
import com.aurora.extensions.browse
import com.aurora.store.R
import com.aurora.store.data.providers.AuthProvider
import com.aurora.store.databinding.FragmentAccountBinding
import com.aurora.store.view.ui.commons.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccountFragment : BaseFragment<FragmentAccountBinding>() {

    @Inject
    lateinit var authProvider: AuthProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Toolbar>(R.id.toolbar)?.apply {
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        // Chips
        view.context.apply {
            binding.chipDisclaimer.setOnClickListener { browse(URL_DISCLAIMER) }
            binding.chipLicense.setOnClickListener { browse(URL_LICENSE) }
            binding.chipTos.setOnClickListener { browse(URL_TOS) }
        }

        authProvider.authData?.userProfile?.let {
            val avatar = if (authProvider.isAnonymous) {
                R.drawable.ic_account_anonymous
            } else {
                it.artwork.url
            }
            binding.imgAvatar.load(avatar) {
                placeholder(R.drawable.bg_placeholder)
                transformations(CircleCropTransformation())
            }
            binding.txtName.text = if (authProvider.isAnonymous) {
                getString(R.string.account_anonymous)
            } else {
                it.name
            }
            if (authProvider.isAnonymous) {
                binding.txtEmail.visibility = View.GONE
            } else {
                binding.txtEmail.text = it.email.lowercase()
            }

        }

        binding.btnLogout.addOnClickListener {
            findNavController().navigate(R.id.logoutDialog)
        }
    }

}
