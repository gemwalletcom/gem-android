package com.gemwallet.android.features.create_wallet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.features.create_wallet.views.CreateWalletScreen

const val createWalletRoute = "create_wallet"

fun NavController.navigateToCreateWalletScreen(navOptions: NavOptions? = null) {
    navigate(createWalletRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.createWalletScreen(
    onCancel: () -> Unit,
    onCreated: () -> Unit,
) {
    composable(createWalletRoute) {
        CreateWalletScreen(
            onCancel = onCancel,
            onCreated = onCreated,
        )
    }
}