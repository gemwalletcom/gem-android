package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.features.referral.views.ReferralNavScreen
import kotlinx.serialization.Serializable

@Serializable
object Referral

fun NavController.navigateToReferralScreen() {
    navigate(Referral, navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.referral(
    onClose: () -> Unit,
) {
    composable<Referral> {
        ReferralNavScreen(onClose)
    }
}