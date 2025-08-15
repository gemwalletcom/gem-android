package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.model.AmountParams
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.features.asset_select.presents.views.SelectSendScreen
import com.gemwallet.features.transfer_amount.presents.views.AmountScreen
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

@Serializable
data class AmountRoute(val params: String)

@Serializable
object SelectSendAssetRoute

fun NavController.navigateToAmountScreen(params: AmountParams) {
    navigate(
        route = AmountRoute(params.pack() ?: return),
        navOptions = navOptions { launchSingleTop = true }
    )
}

fun NavController.navigateToSendScreen(assetId: AssetId? = null, navOptions: NavOptions? = null) {
    if (assetId == null) {
        navigate(route = SelectSendAssetRoute, navOptions ?: navOptions { launchSingleTop = true })
    } else {
        val params = AmountParams.buildTransfer(assetId, destination = null, memo = "").pack() ?: return
        navigate(
            route = AmountRoute(params),
            navOptions = navOptions { launchSingleTop = true }
        )
    }
}

fun NavGraphBuilder.amount(
    onCancel: () -> Unit,
    onSend: (AssetId) -> Unit,
    onConfirm: (ConfirmParams) -> Unit,
) {
    composable<SelectSendAssetRoute> {
        SelectSendScreen(
            onCancel = onCancel,
            onSelect = {
                onSend(it)
            }
        )
    }

    composable<AmountRoute> {
        AmountScreen(onCancel = onCancel, onConfirm = onConfirm)
    }
}