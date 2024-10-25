package com.gemwallet.android.features.asset.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.asset.chart.views.AssetChartScene
import com.wallet.core.primitives.AssetId

const val assetChartRoute = "asset/chart"

fun NavController.navigateToAssetChartScreen(assetId: AssetId, navOptions: NavOptions? = null) {
    navigate("$assetChartRoute/${assetId.toIdentifier().urlEncode()}", navOptions ?: navOptions {
        launchSingleTop = true
    })
}

fun NavGraphBuilder.assetChartScreen(
    onCancel: () -> Unit,
) {
    composable(
        "$assetChartRoute/{$assetIdArg}",
        arguments = listOf(
            navArgument(assetIdArg) { nullable = true},
        )
    ) {
        val assetId = it.arguments?.getString(assetIdArg)?.urlDecode()?.toAssetId()
        if (assetId == null) {
            onCancel()
        } else {
            AssetChartScene(
                assetId = assetId,
                onCancel = onCancel,
            )
        }
    }
}