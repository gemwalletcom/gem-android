package com.gemwallet.android.features.swap.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.swap.views.SwapScreen
import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain

internal const val fromArg = "from"
internal const val toArg = "to"
const val swapRoute = "swap"

fun NavController.navigateToSwap(
    from: AssetId = AssetId(Chain.Ethereum),
    to: AssetId = AssetId(Chain.Ethereum, "0xdAC17F958D2ee523a2206206994597C13D831ec7")
) {
    navigate("$swapRoute/${from.toIdentifier().urlEncode()}/${to.toIdentifier().urlEncode()}")
}

fun NavGraphBuilder.swap(
    onConfirm: (ConfirmParams) -> Unit,
    onCancel: () -> Unit,
) {
    composable(
        route = "$swapRoute/{$fromArg}/{$toArg}",
        arguments = listOf(
            navArgument(fromArg) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(toArg) {
                type = NavType.StringType
                nullable = false
            },
        )
    ) { entry ->
        val fromAssetId = entry.arguments?.getString(fromArg)?.urlDecode()?.toAssetId()
        val toAssetId = entry.arguments?.getString(toArg)?.urlDecode()?.toAssetId()
        if (fromAssetId == null || toAssetId == null) {
            onCancel()
            return@composable
        }
        SwapScreen(
            fromAssetId = fromAssetId,
            toAssetId = toAssetId,
            onConfirm = onConfirm,
            onCancel = onCancel,
        )
    }
}