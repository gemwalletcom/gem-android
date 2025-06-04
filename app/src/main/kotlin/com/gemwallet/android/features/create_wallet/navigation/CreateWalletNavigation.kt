package com.gemwallet.android.features.create_wallet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.features.create_wallet.views.CreateWalletScreen
import com.gemwallet.android.features.create_wallet.views.PhraseAlertDialog
import kotlinx.serialization.Serializable

@Serializable
data class CreateWalletRoute(val isAcceptedRules: Boolean)

fun NavController.navigateToCreateWalletScreen(isAcceptedRules: Boolean = false, navOptions: NavOptions? = null) {
    navigate(CreateWalletRoute(isAcceptedRules), navOptions ?: navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.createWalletScreen(
    onCreateWallet: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onCreated: () -> Unit,
) {
    composable<CreateWalletRoute> {
        if (it.arguments?.getBoolean("isAcceptedRules") == true) {
            CreateWalletScreen(
                onCancel = onCancel,
                onCreated = onCreated,
            )
        } else {
            PhraseAlertDialog({ onCreateWallet(true) }, onCancel)
        }
    }
}