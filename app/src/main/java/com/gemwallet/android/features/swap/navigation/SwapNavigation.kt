package com.gemwallet.android.features.swap.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.swap.views.SwapScreen
import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.AssetId

internal const val pairArg = "pair"
const val swapRoute = "swap"

fun NavController.navigateToSwap(from: AssetId? = null, to: AssetId? = null) {
    navigate("$swapRoute/${from?.toIdentifier()?.urlEncode()}|${to?.toIdentifier()?.urlEncode()}")
}

fun NavGraphBuilder.swap(
    onConfirm: (ConfirmParams) -> Unit,
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
            onCancel = onCancel,
        )
    }
}