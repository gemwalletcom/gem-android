package com.gemwallet.android.ui.navigation.routes

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.features.wallet.presents.PhraseScreen
import com.gemwallet.android.features.wallet.presents.WalletScreen
import com.gemwallet.android.model.AuthRequest
import com.gemwallet.android.ui.requestAuth
import kotlinx.serialization.Serializable


@Serializable
data class WalletDetailsRoute(val walletId: String)

@Serializable
data class WalletPhraseRoute(val walletId: String, val inPhrase: Boolean = true)

fun NavController.navigateToWalletScreen(walletId: String, navOptions: NavOptions? = null) {
    navigate(WalletDetailsRoute(walletId), navOptions ?: navOptions {launchSingleTop = true})
}

fun NavController.navigateToPhraseScreen(walletId: String, navOptions: NavOptions? = null) {
    navigate(WalletPhraseRoute(walletId), navOptions ?: navOptions {launchSingleTop = true})
}

fun NavGraphBuilder.walletScreen(
    onBoard: () -> Unit,
    onCancel: () -> Unit,
    onPhraseShow: (String) -> Unit
) {
    composable<WalletDetailsRoute> {
        val context = LocalContext.current

        WalletScreen(
            onAuthRequest = { callback ->
                context.requestAuth(AuthRequest.Phrase) { callback() }
            },
            onPhraseShow = onPhraseShow,
            onBoard = onBoard,
            onCancel = onCancel,
        )
    }

    composable<WalletPhraseRoute> {
        PhraseScreen(onCancel = onCancel)
    }
}