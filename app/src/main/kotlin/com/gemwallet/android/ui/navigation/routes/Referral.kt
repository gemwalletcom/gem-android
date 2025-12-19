package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.gemwallet.features.referral.views.ReferralNavScreen
import kotlinx.serialization.Serializable

const val referralRouteUriHttp = "https://gemwallet.com/join"
const val referralRouteUriGem = "gem://join"

@Serializable
data class Referral(val code: String? = null)

fun NavController.navigateToReferralScreen() {
    navigate(Referral(), navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.referral(
    onClose: () -> Unit,
) {
    composable<Referral>(
        deepLinks = listOf(
            navDeepLink<Referral>(basePath = referralRouteUriGem),
            navDeepLink<Referral>(basePath = referralRouteUriHttp),
        )
    ) {
        ReferralNavScreen(onClose)
    }
}