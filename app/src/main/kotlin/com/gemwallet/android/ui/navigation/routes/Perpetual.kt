package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.features.perpetual.views.market.PerpetualMarketNavScreen
import com.gemwallet.features.perpetual.views.position.PerpetualPositionNavScreen
import kotlinx.serialization.Serializable

@Serializable
object PerpetualRoute

@Serializable
data class PerpetualPositionRoute(val perpetualId: String)

fun NavController.navigateToPerpetualsScreen(navOptions: NavOptions? = null) {
    navigate(PerpetualRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToPerpetualDetailsScreen(perpetualId: String, navOptions: NavOptions? = null) {
    navigate(PerpetualPositionRoute(perpetualId), navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.perpetualScreen(
    onCancel: () -> Unit,
    onOpenPerpetualDetails: (String) -> Unit,
) {
    composable<PerpetualRoute> {
        PerpetualMarketNavScreen(
            onOpenPerpetualDetails = onOpenPerpetualDetails,
            onCancel = onCancel
        )
    }

    composable<PerpetualPositionRoute> {
        PerpetualPositionNavScreen(
            onClose = onCancel
        )
    }
}