package com.gemwallet.android.features.amount.navigation

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.gemwallet.android.features.amount.models.AmountParams
import com.gemwallet.android.features.amount.views.AmountScreen
import com.gemwallet.android.features.asset_select.views.SelectSendScreen
import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.AssetId

internal const val paramsArg = "params"

const val sendAssetSelectRoute = "sendAssetSelect"
const val sendAmountRoute = "send_amount"

fun interface OnAmount {
    operator fun invoke(params: AmountParams)
}

fun NavController.navigateToAmountScreen(params: AmountParams) {
    navigate(
        route = "$sendAmountRoute?$paramsArg=${params.pack()}",
        navOptions = navOptions { launchSingleTop = true }
    )
}

fun NavController.navigateToSendScreen(assetId: AssetId? = null, navOptions: NavOptions? = null) {
    if (assetId == null) {
        navigate(sendAssetSelectRoute, navOptions ?: navOptions { launchSingleTop = true })
    } else {
        val params = AmountParams.buildTransfer(assetId, destination = null, memo = "").pack()
        navigate("$sendAmountRoute?$paramsArg=${params}", navOptions ?: navOptions { launchSingleTop = true })
    }
}

@OptIn(ExperimentalGetImage::class)
fun NavGraphBuilder.amount(
    onCancel: () -> Unit,
    onSend: (AssetId) -> Unit,
    onConfirm: (ConfirmParams) -> Unit,
) {

    composable(route = sendAssetSelectRoute) {
        SelectSendScreen(
            onCancel = onCancel,
            onSelect = {
                onSend(it)
            }
        )
    }

    composable(
        route = "$sendAmountRoute?$paramsArg={$paramsArg}",
        arguments = listOf(
            navArgument(paramsArg) {
                type = NavType.StringType
                nullable = false
            },
        )
    ) {
        AmountScreen(onCancel = onCancel, onConfirm = onConfirm)
    }
}