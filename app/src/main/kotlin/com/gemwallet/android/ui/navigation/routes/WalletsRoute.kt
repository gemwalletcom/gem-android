package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.features.wallets.presents.views.WalletsScreen
import kotlinx.serialization.Serializable

@Serializable
object WalletsRoute

fun NavController.navigateToWalletsScreen(navOptions: NavOptions? = null) {
    navigate(WalletsRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.walletsScreen(
    onCancel: () -> Unit,
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit,
    onEditWallet: (String) -> Unit,
    onSelectWallet: () -> Unit,
    onBoard: () -> Unit,
) {
    composable<WalletsRoute> {
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