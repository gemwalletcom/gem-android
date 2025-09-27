package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.features.bridge.presents.ConnectionScene
import com.gemwallet.android.features.bridge.presents.ConnectionsScene
import kotlinx.serialization.Serializable

@Serializable
object BridgeConnectionsRoute

@Serializable
data class BridgeConnectionRoute(val connectionId: String)

fun NavController.navigateToBridgesScreen(navOptions: NavOptions? = null) {
    navigate(BridgeConnectionsRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToBridgeScreen(connectionId: String, navOptions: NavOptions? = null) {
    navigate(BridgeConnectionRoute(connectionId), navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.bridgesScreen(
    onConnection: (String) -> Unit,
    onCancel: () -> Unit,
) {
    composable<BridgeConnectionsRoute> {
        ConnectionsScene(
            onConnection = onConnection,
            onCancel = onCancel
        )
    }

    composable<BridgeConnectionRoute> {
        ConnectionScene(onCancel = onCancel)
    }
}