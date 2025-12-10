package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.features.perpetual.views.market.PerpetualMarketNavScreen
import kotlinx.serialization.Serializable

@Serializable
object PerpetualRoute

fun NavController.navigateToPerpetualScreen(navOptions: NavOptions? = null) {
    navigate(PerpetualRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.perpetualScreen(
    onCancel: () -> Unit,
) {
    composable<PerpetualRoute> {
        PerpetualMarketNavScreen(
            onCancel = onCancel
        )
    }
}