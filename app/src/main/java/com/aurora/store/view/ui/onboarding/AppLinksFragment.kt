package com.aurora.store.view.ui.onboarding

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.provider.Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import com.aurora.extensions.isSAndAbove
import com.aurora.extensions.toast
import com.aurora.store.R
import com.aurora.store.databinding.FragmentAppLinksBinding
import com.google.android.material.button.MaterialButton


class AppLinksFragment : Fragment(R.layout.fragment_app_links) {

    private var _binding: FragmentAppLinksBinding? = null
    private val binding get() = _binding!!

    private val playStoreDomain = "play.google.com"
    private val marketDomain = "market.android.com"
    private val amazonAppStoreDomain = "www.amazon.com"

    // AppLink buttons
    private lateinit var buttons: Map<String, MaterialButton>

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (isSAndAbove() && buttons.keys.any { domainVerified(it) }) {
                toast(R.string.onboarding_appsLinks_allEnabled)
            }
            updateButtonState()
        }

    private val playStorePackageName: String = "com.android.vending"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAppLinksBinding.bind(view)

        buttons = mapOf(
            playStoreDomain to binding.playStoreButton,
            marketDomain to binding.marketButton,
            amazonAppStoreDomain to binding.amazonAppStoreButton
        )

        updateButtonState()
        if (isSAndAbove()) {
            buttons.values.forEach {
                it.setOnClickListener {
                    val intent = Intent(
                        ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                        Uri.parse("package:${view.context.packageName}")
                    )
                    startForResult.launch(intent)
                }
            }
        }

        /* Tooltips frame */
        val onboardingAppsLinksTooltips: LinearLayoutCompat =
            view.findViewById(R.id.onboarding_appsLinks_tooltips) // Set ID object
        if (!playStoreIsInstalled()) { // Condition: The answer is negative
            onboardingAppsLinksTooltips.visibility = View.GONE // Action: Hide tooltips layout
            onboardingAppsLinksTooltips.isFocusable = false // Action: Hide tooltips layout
            onboardingAppsLinksTooltips.isClickable = false // Action: Make the element unclickable.
        }

        /* Tooltip link to official documentation */
        val onboardingAppsLinksTooltipButtonDocumentation: MaterialButton =
            view.findViewById(R.id.onboarding_appsLinks_tooltip_buttonDocumentation) // Set ID object
        // Action
        onboardingAppsLinksTooltipButtonDocumentation.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://developer.android.com/training/app-links/verify-android-applinks")
                )
            )
        }

        /* Failures tooltip */
        val onboardingAppsLinksTooltipButtonSettings: MaterialButton =
            view.findViewById(R.id.onboarding_appsLinks_tooltip_buttonSettings) // Set ID object
        onboardingAppsLinksTooltipButtonSettings.setOnClickListener {
            startActivity(Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", playStorePackageName, null)
            })
        }

    }

    override fun onDestroyView() {
        buttons = emptyMap()
        _binding = null
        super.onDestroyView()
    }

    private fun updateButtonState() {
        if (isSAndAbove()) {
            buttons.forEach { (domain, button) ->
                if (domainVerified(domain)) {
                    button.apply {
                        text = getString(R.string.action_enabled)
                        isEnabled = false
                    }
                }
            }
        } else {
            buttons.forEach { (_, button) ->
                button.apply {
                    text = getString(R.string.action_enabled)
                    isEnabled = false
                }
            }
        }
    }

    private fun domainVerified(domain: String): Boolean {
        return if (isSAndAbove()) {
            val domainVerificationManager = requireContext().getSystemService(
                DomainVerificationManager::class.java
            )
            val userState = domainVerificationManager.getDomainVerificationUserState(
                requireContext().packageName
            )
            val domainMap = userState?.hostToStateMap?.filterKeys { it == domain }
            domainMap?.values?.first() == DomainVerificationUserState.DOMAIN_STATE_SELECTED
        } else {
            true
        }
    }

    private fun playStoreIsInstalled(): Boolean {
        return try {
            requireContext().packageManager.getPackageInfo(playStorePackageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

}
