package com.gemwallet.android.features.wallets.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.features.wallets.views.WalletsScreen

const val walletsRoute = "wallets"

fun NavController.navigateToWalletsScreen(navOptions: NavOptions? = null) {
    navigate(walletsRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.walletsScreen(
    onCancel: () -> Unit,
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit,
    onEditWallet: (String) -> Unit,
    onSelectWallet: () -> Unit,
    onBoard: () -> Unit,
) {
    composable(walletsRoute) {
        WalletsScreen(
            onCreateWallet = onCreateWallet,
            onImportWallet = onImportWallet,
            onEditWallet = onEditWallet,
            onSelectWallet = onSelectWallet,
            onBoard = onBoard,
            onCancel = onCancel
        )
    }
}