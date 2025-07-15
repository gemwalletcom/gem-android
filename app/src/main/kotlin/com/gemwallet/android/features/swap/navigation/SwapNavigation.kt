package com.gemwallet.android.features.swap.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.swap.views.SwapScreen
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.wallet.core.primitives.AssetId

internal const val pairArg = "pair"
const val swapRoute = "swap"

fun NavController.navigateToSwap(from: AssetId? = null, to: AssetId? = null) {
    navigate(
        "$swapRoute/${from?.toIdentifier()?.urlEncode()}|${to?.toIdentifier()?.urlEncode()}",
        navOptions { launchSingleTop = true },
    )
}

fun NavGraphBuilder.swap(
    onConfirm: (ConfirmParams) -> Unit,
    onBuy: AssetIdAction,
    onCancel: () -> Unit,
) {
    composable(
        route = "$swapRoute/{$pairArg}",
        arguments = listOf(
            navArgument(pairArg) {
                type = NavType.StringType
                nullable = true
            },
        )
    ) {
        SwapScreen(
            onConfirm = onConfirm,
            onBuy = onBuy,
            onCancel = onCancel,
        )
    }
}