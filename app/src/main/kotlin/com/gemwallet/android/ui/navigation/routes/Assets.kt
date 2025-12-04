package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.ui.components.animation.enterTabScreenTransition
import com.gemwallet.android.ui.components.animation.exitTabScreenTransition
import com.gemwallet.features.assets.views.AssetsScreen
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

@Serializable
object AssetsRoute

fun NavController.navigateToAssetsScreen(navOptions: NavOptions? = null) {
    navigate(AssetsRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.assetsScreen(
    onShowWallets: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSwapClick: (AssetId?) -> Unit,
    onManage: () -> Unit,
    onSearch: () -> Unit,
    onAssetClick: (AssetId) -> Unit,
) {
    composable<AssetsRoute>(
        enterTransition = enterTabScreenTransition,
        exitTransition = exitTabScreenTransition,
    ) {
        AssetsScreen(
            onShowWallets = onShowWallets,
            onManage = onManage,
            onSearch = onSearch,
            onSendClick = onSendClick,
            onReceiveClick = onReceiveClick,
            onBuyClick = onBuyClick,
            onSwapClick = onSwapClick,
            onAssetClick = onAssetClick,
        )
    }
}