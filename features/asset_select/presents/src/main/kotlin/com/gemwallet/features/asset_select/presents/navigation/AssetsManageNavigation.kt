package com.gemwallet.android.features.create_wallet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.features.asset_select.presents.views.AssetsManageScreen

const val assetsManageRoute = "manage_assets"

fun NavController.navigateToAssetsManageScreen(navOptions: NavOptions? = null) {
    navigate(assetsManageRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.assetsManageScreen(
    onAddAsset: () -> Unit,
    onCancel: () -> Unit,
) {
    composable(assetsManageRoute) {
        AssetsManageScreen(
            onAddAsset = onAddAsset,
            onCancel = onCancel,
        )
    }
}