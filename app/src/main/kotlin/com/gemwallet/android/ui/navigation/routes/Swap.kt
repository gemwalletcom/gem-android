package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.features.swap.views.SwapScreen
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

@Serializable
data class SwapRoute(val from: String?, val to: String?)

fun NavController.navigateToSwap(from: AssetId? = null, to: AssetId? = null) {
    val route = SwapRoute(from?.toIdentifier(), to?.toIdentifier())
    navigate(
        route = route,
        navOptions = navOptions { launchSingleTop = true },
    )
}

fun NavGraphBuilder.swap(
    onConfirm: (ConfirmParams) -> Unit,
    onCancel: () -> Unit,
) {
    composable<SwapRoute> {
        SwapScreen(
            onConfirm = onConfirm,
            onCancel = onCancel,
        )
    }
}