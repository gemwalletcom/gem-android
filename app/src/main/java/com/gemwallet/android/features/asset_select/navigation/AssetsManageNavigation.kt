package com.gemwallet.android.features.create_wallet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.gemwallet.android.features.asset_select.views.AssetsManageScreen

const val assetsManageRoute = "manage_assets"

fun NavController.navigateToAssetsManageScreen(navOptions: NavOptions? = null) {
    navigate(assetsManageRoute, navOptions)
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