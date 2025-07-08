package com.gemwallet.android.features.confirm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.gemwallet.android.features.confirm.views.ConfirmScreen
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.models.actions.FinishConfirmAction

internal const val paramsArg = "assetId"
internal const val txTypeArg = "tx_type"

const val txConfirmRoute = "tx_confirm"

fun NavController.navigateToConfirmScreen(params: ConfirmParams) {
    val txType = params.getTxType()
    navigate(
        route = "$txConfirmRoute?$paramsArg=${params.pack()}&$txTypeArg=${txType.string}",
        navOptions = navOptions { launchSingleTop = true },
    )
}

fun NavGraphBuilder.confirm(
    finishAction: FinishConfirmAction,
    onBuy: AssetIdAction,
    cancelAction: CancelAction,
) {
    composable(
        route = "$txConfirmRoute?$paramsArg={$paramsArg}&$txTypeArg={$txTypeArg}",
        arguments = listOf(
            navArgument(paramsArg) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(txTypeArg) {
                type = NavType.StringType
                nullable = false
            },
        )
    ) { entry ->
        ConfirmScreen(
            cancelAction = cancelAction,
            onBuy = onBuy,
            finishAction = finishAction,
        )
    }
}