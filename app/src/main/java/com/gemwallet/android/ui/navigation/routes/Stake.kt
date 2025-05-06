package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.stake.delegation.views.DelegationScene
import com.gemwallet.android.features.stake.stake.views.StakeScreen
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

@Serializable
data class StakeRoute(val assetId: String)

@Serializable
data class DelegationRoute(val validatorId: String, val delegationId: String)

const val stakeRoute = "stake"

fun NavController.navigateToStake(assetId: AssetId) {
    navigate(StakeRoute(assetId.toIdentifier()), navOptions { launchSingleTop = true })
}

fun NavController.navigateToDelegation(validatorId: String, delegationId: String) {
    navigate(DelegationRoute(validatorId, delegationId), navOptions = navOptions { launchSingleTop = true })
}

fun NavGraphBuilder.stake(
    onAmount: AmountTransactionAction,
    onConfirm: (ConfirmParams) -> Unit,
    onDelegation: (String, String) -> Unit,
    onCancel: () -> Unit,
) {
    composable<StakeRoute> { entry ->
        StakeScreen(
            amountAction = onAmount,
            onDelegation = onDelegation,
            onConfirm = onConfirm,
            onCancel = onCancel,
        )
    }

    composable<DelegationRoute> {
        DelegationScene(
            onAmount = onAmount,
            onCancel = onCancel,
        )
    }
}