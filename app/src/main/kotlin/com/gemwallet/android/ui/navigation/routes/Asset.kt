package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.features.asset.presents.chart.views.AssetChartScene
import com.gemwallet.features.asset.presents.details.views.AssetDetailsScreen
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val assetRouteUri = "gem://asset"

@Serializable
data class AssetRoute(
    @SerialName("assetId") val assetId: String
)

@Serializable
data class AssetChartRoute(val assetId: String)

fun NavController.navigateToAssetScreen(assetId: AssetId, navOptions: NavOptions? = null) {
    val route = AssetRoute(assetId.toIdentifier())
    navigate(route, navOptions  ?: navOptions {
        popUpTo(route) { inclusive = true }
    })
}

fun NavController.navigateToAssetChartScreen(assetId: AssetId, navOptions: NavOptions? = null) {
    navigate(AssetChartRoute(assetId.toIdentifier()), navOptions ?: navOptions {
        launchSingleTop = true
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
    onConfirm: (ConfirmParams) -> Unit,
) {
    composable<AssetRoute>(
        deepLinks = listOf(
            navDeepLink<AssetRoute>(basePath = assetRouteUri)
        )
    ) {
        AssetDetailsScreen(
            onCancel = onCancel,
            onTransfer = onTransfer,
            onReceive = onReceive,
            onBuy = onBuy,
            onSwap = onSwap,
            onTransaction = onTransaction,
            onChart = onChart,
            openNetwork = openNetwork,
            onStake = onStake,
            onConfirm = onConfirm,
        )
    }
}

fun NavGraphBuilder.assetChartScreen(
    onCancel: () -> Unit,
) {
    composable< AssetChartRoute> {
        AssetChartScene(
            onCancel = onCancel,
        )
    }
}