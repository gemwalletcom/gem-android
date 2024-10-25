package com.gemwallet.android.features.stake.navigation

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
import com.gemwallet.android.features.amount.navigation.OnAmount
import com.gemwallet.android.features.stake.delegation.views.DelegationScene
import com.gemwallet.android.features.stake.stake.views.StakeScreen
import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.AssetId

internal const val assetIdArg = "asset_id"
internal const val validatorIdArg = "validator_id"
internal const val delegationIdArg = "delegation_id"

const val stakeRoute = "stake"
const val delegationRoute = "delegation"

fun NavController.navigateToStake(assetId: AssetId) {
    navigate("$stakeRoute/${assetId.toIdentifier().urlEncode()}", navOptions { launchSingleTop = true })
}

fun NavController.navigateToDelegation(validatorId: String, delegationId: String) {
    navigate(
        route = "$delegationRoute?$validatorIdArg=${validatorId.urlEncode()}&$delegationIdArg=${delegationId.urlEncode()}",
        navOptions = navOptions { launchSingleTop = true },
    )
}

fun NavGraphBuilder.stake(
    onAmount: OnAmount,
    onConfirm: (ConfirmParams) -> Unit,
    onDelegation: (String, String) -> Unit,
    onCancel: () -> Unit,
) {
    navigation("$stakeRoute/{${assetIdArg}}", stakeRoute) {
        composable(
            route = "$stakeRoute/{$assetIdArg}",
            arguments = listOf(
                navArgument(assetIdArg) {
                    type = NavType.StringType
                    nullable = false
                },
            )
        ) { entry ->
            val assetId = entry.arguments?.getString(assetIdArg)?.urlDecode()?.toAssetId()
            if (assetId == null) {
                onCancel()
                return@composable
            }
            StakeScreen(
                assetId = assetId,
                onAmount = onAmount,
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
}