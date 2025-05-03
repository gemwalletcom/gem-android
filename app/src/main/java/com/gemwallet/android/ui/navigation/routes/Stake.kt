package com.gemwallet.android.ui.navigation.routes

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import androidx.navigation.navigation
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.ext.urlEncode
import com.gemwallet.android.features.stake.delegation.views.DelegationScene
import com.gemwallet.android.features.stake.stake.views.StakeScreen
import com.gemwallet.android.model.ConfirmParams
import com.gemwallet.android.ui.models.actions.AmountTransactionAction
import com.wallet.core.primitives.AssetId
import kotlinx.serialization.Serializable

@Serializable
data class StakeRoute(val assetId: String)

internal const val validatorIdArg = "validator_id"
internal const val delegationIdArg = "delegation_id"

const val stakeRoute = "stake"
const val delegationRoute = "delegation"

fun NavController.navigateToStake(assetId: AssetId) {
    navigate(StakeRoute(assetId.toIdentifier()), navOptions { launchSingleTop = true })
}

fun NavController.navigateToDelegation(validatorId: String, delegationId: String) {
    navigate(
        route = "$delegationRoute?$validatorIdArg=${validatorId.urlEncode()}&$delegationIdArg=${delegationId.urlEncode()}",
        navOptions = navOptions { launchSingleTop = true },
    )
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

    composable(
        route = "$delegationRoute?$validatorIdArg={$validatorIdArg}&$delegationIdArg={$delegationIdArg}",
        arguments = listOf(
            navArgument(validatorIdArg) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(delegationIdArg) {
                type = NavType.StringType
                nullable = true
            }
        ),
    ) {
        val validatorId = it.arguments?.getString(validatorIdArg)
        val delegationId = it.arguments?.getString(delegationIdArg) ?: ""

        if (validatorId == null) {
            onCancel()
            return@composable
        }
        DelegationScene(
            validatorId = validatorId,
            delegationId = delegationId,
            onAmount = onAmount,
            onCancel = onCancel,
        )
    }
}