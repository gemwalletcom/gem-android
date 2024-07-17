package com.gemwallet.android.features.confirm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gemwallet.android.ext.urlDecode
import com.gemwallet.android.features.confirm.views.ConfirmScreen
import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.TransactionType


internal const val paramsArg = "assetId"
internal const val txTypeArg = "tx_type"

const val txConfirmRoute = "tx_confirm"

fun NavController.navigateToConfirmScreen(params: ConfirmParams) {
    val txType = params.getTxType()
    navigate(route = "$txConfirmRoute?$paramsArg=${params.pack()}&$txTypeArg=${txType.string}")
}

fun NavGraphBuilder.confirm(
    onFinish: (String) -> Unit,
    onCancel: () -> Unit,
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
        val paramsPack = entry.arguments?.getString(paramsArg)
        val txTypeString = entry.arguments?.getString(txTypeArg)?.urlDecode()
        val txType = TransactionType.entries.firstOrNull { it.string == txTypeString }

        if (txType == null || paramsPack == null) {
            onCancel()
            return@composable
        }
        val params = ConfirmParams.unpack(
            when (txType) {
                TransactionType.Transfer -> ConfirmParams.TransferParams::class.java
                TransactionType.Swap -> ConfirmParams.SwapParams::class.java
                TransactionType.TokenApproval -> ConfirmParams.TokenApprovalParams::class.java
                TransactionType.StakeDelegate -> ConfirmParams.DelegateParams::class.java
                TransactionType.StakeUndelegate -> ConfirmParams.UndelegateParams::class.java
                TransactionType.StakeRewards -> ConfirmParams.RewardsParams::class.java
                TransactionType.StakeRedelegate -> ConfirmParams.RedeleateParams::class.java
                TransactionType.StakeWithdraw -> ConfirmParams.WithdrawParams::class.java
            },
            paramsPack,
        )

        if (params == null) {
            onCancel()
            return@composable
        }

        ConfirmScreen(
            params = params,
            onCancel = onCancel,
            onFinish = onFinish,
        )
    }
}