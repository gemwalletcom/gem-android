package com.gemwallet.android.features.receive.navigation

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
import com.gemwallet.android.features.asset_select.views.SelectReceiveScreen
import com.gemwallet.android.features.receive.views.ReceiveScreen
import com.wallet.core.primitives.AssetId

internal const val assetIdArg = "assetId"
const val receiveRoute = "receiveAsset"
const val receiveAssetSelectRoute = "receiveAssetSelect"

fun NavController.navigateToReceiveScreen(assetId: AssetId? = null, navOptions: NavOptions? = null) {
    if (assetId == null) {
        navigate(receiveAssetSelectRoute, navOptions ?: navOptions { launchSingleTop = true })
    } else {
        navigate("$receiveRoute/${assetId.toIdentifier().urlEncode()}", navOptions ?: navOptions { launchSingleTop = true })
    }

}

fun NavGraphBuilder.receiveScreen(
    onCancel: () -> Unit,
    onReceive: (AssetId) -> Unit,
) {
    navigation("$receiveRoute/{$assetIdArg}", receiveRoute) {
        composable(
            route = "$receiveRoute/{$assetIdArg}",
            arguments = listOf(
                navArgument(assetIdArg) {
                    type = NavType.StringType
                    nullable = true
                }
            ),
        ) {
            val assetId = it.arguments?.getString(assetIdArg)?.urlDecode()?.toAssetId()
            if (assetId == null) {
                onCancel()
            } else {
                ReceiveScreen(onCancel = onCancel)
            }
        }

        composable(receiveAssetSelectRoute) {
            SelectReceiveScreen(
                onCancel = onCancel,
                onSelect = {
                    onReceive(it)
                }
            )
        }
    }
}