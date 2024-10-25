package com.gemwallet.android.features.buy.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.asset_select.views.SelectBuyScreen
import com.gemwallet.android.features.buy.views.BuyScreen
import com.wallet.core.primitives.AssetId

internal const val assetIdArg = "assetId"
const val buyRoute = "buy"
const val buySelectAssetRoute = "buySelectAsset"

fun NavController.navigateToBuyScreen(assetId: AssetId? = null, navOptions: NavOptions? = null) {
    if (assetId == null) {
        navigate(buySelectAssetRoute, navOptions ?: navOptions { launchSingleTop = true })
    } else {
        navigate("$buyRoute/${assetId.toIdentifier().urlEncode()}", navOptions ?: navOptions { launchSingleTop = true })
    }
}

fun NavGraphBuilder.buyScreen(
    onCancel: () -> Unit,
    onBuy: (AssetId) -> Unit,
) {
    navigation("$buyRoute/{$assetIdArg}", buyRoute) {
        composable(
            route = "$buyRoute/{$assetIdArg}",
            arguments = listOf(
                navArgument(assetIdArg) {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) {
            val assetId = it.arguments?.getString(assetIdArg)?.urlDecode()?.toAssetId()
            if (assetId == null) {
                onCancel()
            } else {
                BuyScreen(
                    assetId = assetId,
                    onCancel = onCancel
                )
            }
        }

        composable(buySelectAssetRoute) {
            SelectBuyScreen(
                onCancel = onCancel,
                onSelect = {
                    onBuy(it)
                }
            )
        }
    }
}