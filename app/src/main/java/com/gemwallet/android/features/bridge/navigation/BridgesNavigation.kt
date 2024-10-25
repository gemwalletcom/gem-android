package com.gemwallet.android.features.bridge.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.bridge.connection.views.ConnectionScene
import com.gemwallet.android.features.bridge.connections.views.ConnectionsScene

internal val connectionIdArg = "connection_id"

const val bridgeRoute = "bridge"
const val bridgesRoute = "bridges"

fun NavController.navigateToBridgesScreen(navOptions: NavOptions? = null) {
    navigate(bridgesRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToBridgeScreen(connectionId: String, navOptions: NavOptions? = null) {
    navigate("$bridgeRoute/${connectionId.urlEncode()}", navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.bridgesScreen(
    onConnection: (String) -> Unit,
    onCancel: () -> Unit,
) {
    composable(bridgesRoute) {
        ConnectionsScene(
            onConnection = onConnection,
            onCancel = onCancel
        )
    }

    composable(
        route = "$bridgeRoute/{$connectionIdArg}",
        arguments = listOf(
            navArgument(connectionIdArg) {
                type = NavType.StringType
                nullable = false
            }
        ),
    ) {
        val connectionId = it.arguments?.getString(connectionIdArg)?.urlDecode()
        if (connectionId == null) {
            onCancel()
            return@composable
        }
        ConnectionScene(connectionId = connectionId, onCancel = onCancel)
    }
}