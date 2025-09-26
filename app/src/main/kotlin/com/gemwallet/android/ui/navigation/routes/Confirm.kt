package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.android.ui.models.actions.CancelAction
import com.gemwallet.android.ui.models.actions.FinishConfirmAction
import com.gemwallet.features.confirm.views.ConfirmScreen
import kotlinx.serialization.Serializable

@Serializable
data class ConfirmRoute(
    val txType: String,
    val data: String,
)

fun NavController.navigateToConfirmScreen(params: ConfirmParams) {
    val route = ConfirmRoute(
        txType = params.getTxType().string,
        data = params.pack() ?: return,
    )
    navigate(
        route = route,
        navOptions = navOptions { launchSingleTop = true },
    )
}

fun NavGraphBuilder.confirm(
    finishAction: FinishConfirmAction,
    onBuy: AssetIdAction,
    cancelAction: CancelAction,
) {
    composable<ConfirmRoute> { entry ->
        ConfirmScreen(
            cancelAction = cancelAction,
            onBuy = onBuy,
            finishAction = finishAction,
        )
    }
}