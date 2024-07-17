package com.gemwallet.android.features.amount.navigation

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gemwallet.android.features.amount.model.AmountParams
import com.gemwallet.android.features.amount.views.AmountScreen
import com.gemwallet.android.model.ConfirmParams

internal const val paramsArg = "params"

const val sendAmountRoute = "send_amount"

fun interface OnAmount {
    operator fun invoke(params: AmountParams)
}

fun NavController.navigateToAmountScreen(params: AmountParams) {
    navigate(route = "$sendAmountRoute?$paramsArg=${params.pack()}")
}

@OptIn(ExperimentalGetImage::class)
fun NavGraphBuilder.amount(
    onCancel: () -> Unit,
    onConfirm: (ConfirmParams) -> Unit,
) {
    composable(
        route = "$sendAmountRoute?$paramsArg={$paramsArg}",
        arguments = listOf(
            navArgument(paramsArg) {
                type = NavType.StringType
                nullable = false
            },
        )
    ) {
        AmountScreen(onCancel = onCancel, onConfirm = onConfirm)
    }
}