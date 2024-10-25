package com.gemwallet.android.features.assets.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.features.assets.views.AssetsScreen
import com.gemwallet.android.ui.navigation.enterTabScreenTransition
import com.gemwallet.android.ui.navigation.exitTabScreenTransition
import com.wallet.core.primitives.AssetId

const val assetsRoute = "assets"

fun NavController.navigateToAssetsScreen(navOptions: NavOptions? = null) {
    navigate(assetsRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.assetsScreen(
    onShowWallets: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSwapClick: () -> Unit,
    onShowAssetManage: () -> Unit,
    onAssetClick: (AssetId) -> Unit,
) {
    composable(
        route = assetsRoute,
        enterTransition = enterTabScreenTransition,
        exitTransition = exitTabScreenTransition,
    ) {
        AssetsScreen(
            onShowWallets = onShowWallets,
            onShowAssetManage = onShowAssetManage,
            onSendClick = onSendClick,
            onReceiveClick = onReceiveClick,
            onBuyClick = onBuyClick,
            onSwapClick = onSwapClick,
            onAssetClick = onAssetClick,
        )
    }
}