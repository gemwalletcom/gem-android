package com.gemwallet.android.ui.navigation.routes

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.features.swap.viewmodels.models.SwapItemType
import com.gemwallet.features.swap.views.SwapScreen
import com.gemwallet.features.swap.views.SwapSelectScreen
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

@Serializable
data class SwapRoute(val from: String?, val to: String?)

@Serializable
data class SwapSelectRoute(
    val select: SwapItemType,
    val payAssetId: String?,
    val receiveAssetId: String?,
)

fun NavController.navigateToSwapSelect(select: SwapItemType, payAssetId: AssetId?, receiveAssetId: AssetId?) {
    val route = SwapSelectRoute(select, payAssetId?.toIdentifier(), receiveAssetId?.toIdentifier())
    navigate(
        route = route,
        navOptions = navOptions { launchSingleTop = true },
    )
}

fun NavController.navigateToSwap(from: AssetId? = null, to: AssetId? = null) {
    val route = SwapRoute(from?.toIdentifier(), to?.toIdentifier())
    navigate(
        route = route,
        navOptions = navOptions { launchSingleTop = true },
    )
}

fun NavGraphBuilder.swapSelect(navController: NavController, onCancel: () -> Unit) {
    composable<SwapSelectRoute> {
        SwapSelectScreen(
            onCancel,
            { select, pay, receive ->
                navController.previousBackStackEntry?.savedStateHandle?.let {
                    it.set("from", pay?.toIdentifier())
                    it.set("to", receive?.toIdentifier())
                    it.set("select", select)
                }
                navController.popBackStack()
            }
        )
    }
}

fun NavGraphBuilder.swap(
    onConfirm: (ConfirmParams) -> Unit,
    onSelect: (select: SwapItemType, payAssetId: AssetId?, receiveAssetId: AssetId?) -> Unit,
    onCancel: () -> Unit,
) {
    composable<SwapRoute> { entry ->
        SwapScreen(
            payId = entry.savedStateHandle.get<String?>("from")?.toAssetId(),
            receiveId = entry.savedStateHandle.get<String?>("to")?.toAssetId(),
            select = entry.savedStateHandle.get("select"),
            onConfirm = onConfirm,
            onSelect = onSelect,
            onCancel = onCancel,
        )
    }
}