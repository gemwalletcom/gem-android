package com.gemwallet.android.features.wallet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.wallet.WalletScreen
import com.gemwallet.android.features.wallet.phrase.views.PhraseScreen

internal const val walletIdArg = "walletId"

const val walletRoute = "wallet"
const val phraseRoute = "phrase"

fun NavController.navigateToWalletScreen(walletId: String, navOptions: NavOptions? = null) {
    navigate("$walletRoute/${walletId.urlEncode()}", navOptions)
}

fun NavController.navigateToPhraseScreen(walletId: String, navOptions: NavOptions? = null) {
    navigate("$phraseRoute/${walletId.urlEncode()}", navOptions)
}

fun NavGraphBuilder.walletScreen(
    onBoard: () -> Unit,
    onCancel: () -> Unit,
    onPhraseShow: (String) -> Unit
) {
    composable(
        "$walletRoute/{$walletIdArg}",
        arguments = listOf(
            navArgument(walletIdArg) {
                type = NavType.StringType
            }
        ),
    ) {
        val walletId = it.arguments?.getString(walletIdArg)?.urlDecode()
        if (walletId == null) {
            onCancel()
            return@composable
        }
        WalletScreen(
            walletId = walletId,
            isPhrase = false,
            onPhraseShow = onPhraseShow,
            onBoard = onBoard,
            onCancel = onCancel,
        )
    }

    composable(
        "$phraseRoute/{$walletIdArg}",
        arguments = listOf(
            navArgument(walletIdArg) {
                type = NavType.StringType
            }
        ),
    ) {
        val walletId = it.arguments?.getString(walletIdArg)?.urlDecode()
        if (walletId == null) {
            onCancel()
            return@composable
        }
        PhraseScreen(
            walletId = walletId,
            onCancel = onCancel,
        )
    }
}