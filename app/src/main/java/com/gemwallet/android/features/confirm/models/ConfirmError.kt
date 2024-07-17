package com.gemwallet.android.features.confirm.models

import androidx.compose.runtime.Composable
import com.gemwallet.android.R

sealed class ConfirmError(message: String) : Exception(message){

    data object None : ConfirmError("")

    class Init(message: String) : ConfirmError(message)

    data object CalculateFee : ConfirmError("Calculate fee error")

    data object TransactionIncorrect : ConfirmError("Transaction data incorrect")

    data object WalletNotAvailable : ConfirmError("Wallet not available")

    class InsufficientBalance(val chainTitle: String) : ConfirmError("Insufficient Balance")

    class InsufficientFee(val chainTitle: String) : ConfirmError("Insufficient Fee")

    class SignFail(message: String) : ConfirmError(message)

    class BroadcastError(message: String) : ConfirmError(message)

    @Composable
    fun stringResource(): String {
        return when (this) {
            None -> androidx.compose.ui.res.stringResource(id = R.string.transfer_confirm)
            is InsufficientBalance -> androidx.compose.ui.res.stringResource(
                id = R.string.transfer_insufficient_balance,
                chainTitle,
            )
            is InsufficientFee -> androidx.compose.ui.res.stringResource(
                id = R.string.transfer_insufficient_network_fee_balance,
                chainTitle,
            )
            is Init,
            is BroadcastError,
            is SignFail,
            TransactionIncorrect,
            WalletNotAvailable -> androidx.compose.ui.res.stringResource(
                id = R.string.errors_transfer,
                message ?: ""
            )
            CalculateFee -> androidx.compose.ui.res.stringResource(R.string.confirm_fee_error)
        }
    }
}