package com.gemwallet.android.features.asset.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.asset.details.views.AssetDetailsScene
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.wallet.core.primitives.AssetId

internal const val assetIdArg = "assetId"
const val assetRoute = "asset"

fun NavController.navigateToAssetScreen(assetId: AssetId, navOptions: NavOptions? = null) {
    val route = "$assetRoute/${assetId.toIdentifier().urlEncode()}"
    navigate(route, navOptions  ?: androidx.navigation.navOptions {
        popUpTo(route) {
            inclusive = true
        }
    })
}

fun NavGraphBuilder.assetScreen(
    onCancel: () -> Unit,
    onTransfer: AssetIdAction,
    onReceive: (AssetId) -> Unit,
    onBuy: (AssetId) -> Unit,
    onSwap: (AssetId?, AssetId?) -> Unit,
    onTransaction: (txId: String) -> Unit,
    onChart: (AssetId) -> Unit,
    openNetwork: AssetIdAction,
    onStake: (AssetId) -> Unit,
) {
    composable(
        "$assetRoute/{$assetIdArg}",
        arguments = listOf(
            navArgument(assetIdArg) { nullable = true},
        )
    ) {
        val assetId = it.arguments?.getString(assetIdArg)?.urlDecode()?.toAssetId()
        if (assetId == null) {
            onCancel()
        } else {
            AssetDetailsScene(
                assetId = assetId,
                onCancel = onCancel,
                onTransfer = onTransfer,
                onReceive = onReceive,
                onBuy = onBuy,
                onSwap = onSwap,
                onTransaction = onTransaction,
                onChart = onChart,
                openNetwork = openNetwork,
                onStake = onStake,
            )
        }
    }
}