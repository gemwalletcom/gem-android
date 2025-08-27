package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.features.add_asset.views.AddAssetScree
import kotlinx.serialization.Serializable

@Serializable
object AddAssetRoute

fun NavController.navigateToAddAssetScreen(navOptions: NavOptions? = null) {
    navigate(AddAssetRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.addAssetScreen(
    onCancel: () -> Unit,
    onFinish: () -> Unit,
) {
    composable<AddAssetRoute> {
        AddAssetScree(onCancel = onCancel, onFinish = onFinish)
    }
}