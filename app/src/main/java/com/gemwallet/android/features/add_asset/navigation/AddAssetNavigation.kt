package com.gemwallet.android.features.add_asset.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.features.add_asset.views.AddAssetScree

const val addAssetRoute = "add_asset"

fun NavController.navigateToAddAssetScreen(navOptions: NavOptions? = null) {
    navigate(addAssetRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.addAssetScreen(
    onCancel: () -> Unit,
    onFinish: () -> Unit,
) {
    composable(
        route = addAssetRoute,
    ) {
        AddAssetScree(onCancel = onCancel, onFinish = onFinish)
    }
}