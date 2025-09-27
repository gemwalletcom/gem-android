package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.gemwallet.android.features.activities.presents.details.TransactionDetails
import com.gemwallet.android.features.activities.presents.list.TransactionsScreen
import com.gemwallet.android.ui.components.animation.enterTabScreenTransition
import com.gemwallet.android.ui.components.animation.exitTabScreenTransition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val transactionsRoute = "transactions"
const val transactionRouteUri = "gem://transaction"

@Serializable
object ActivitiesRoute

@Serializable
data class TransactionDetailsRoute(
    @SerialName("txId") val txId: String
)

fun NavController.navigateToActivitiesScreen(navOptions: NavOptions? = null) {
    navigate(ActivitiesRoute, navOptions ?: navOptions { launchSingleTop = true })
}

fun NavController.navigateToTransactionScreen(txId: String, navOptions: NavOptions? = null) {
    navigate(TransactionDetailsRoute(txId), navOptions ?: navOptions {
        launchSingleTop = true
    })
}

fun NavGraphBuilder.activitiesScreen(
    onTransaction: (String) -> Unit,
) {
    composable<ActivitiesRoute>(
        enterTransition = enterTabScreenTransition,
        exitTransition = exitTabScreenTransition,
    ) {
        TransactionsScreen(onTransaction = onTransaction)
    }
}

fun NavGraphBuilder.transactionDetailsScreen(
    onCancel: () -> Unit,
) {
    composable<TransactionDetailsRoute>(
        deepLinks = listOf(
            navDeepLink<TransactionDetailsRoute>(basePath = transactionRouteUri)
        )
    ) {
        TransactionDetails(onCancel = onCancel)
    }
}