package com.gemwallet.android.features.activities.presents

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.activities.presents.details.TransactionDetails
import com.gemwallet.android.features.activities.presents.list.views.TransactionsScreen
import com.gemwallet.android.features.activities.viewmodels.txIdArg
import com.gemwallet.android.ui.components.animation.enterTabScreenTransition
import com.gemwallet.android.ui.components.animation.exitTabScreenTransition

const val activitiesRoute = "transactions"
const val transactionRoute = "transaction"

fun NavController.navigateToActivitiesScreen(navOptions: NavOptions? = null) {
    navigate(activitiesRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToTransactionScreen(txId: String, navOptions: NavOptions? = null) {
    navigate("$transactionRoute/${txId.urlEncode()}", navOptions ?: navOptions {
        launchSingleTop = true
    })
}

fun NavGraphBuilder.activitiesScreen(
    onTransaction: (String) -> Unit,
) {
    composable(
        route = activitiesRoute,
        enterTransition = enterTabScreenTransition,
        exitTransition = exitTabScreenTransition,
    ) {
        TransactionsScreen(
            onTransaction = onTransaction,
        )
    }
}

fun NavGraphBuilder.transactionScreen(
    onCancel: () -> Unit,
) {
    composable(
        "$transactionRoute/{$txIdArg}",
        arguments = listOf(
            navArgument(txIdArg) {
                type = NavType.StringType
            },
        )
    ) {
        if (it.arguments?.getString(txIdArg)?.urlDecode().isNullOrEmpty()) {
            onCancel()
        } else {
            TransactionDetails(onCancel = onCancel)
        }
    }
}