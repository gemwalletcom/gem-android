package com.gemwallet.android.features.confirm.models

sealed class ConfirmError(message: String) : Exception(message){

    data object None : ConfirmError("")

    class Init(message: String) : ConfirmError(message)

    data object CalculateFee : ConfirmError("Calculate fee error")

    data object TransactionIncorrect : ConfirmError("Transaction data incorrect")

    class InsufficientBalance(val chainTitle: String) : ConfirmError("Insufficient Balance")

    class InsufficientFee(val chainTitle: String) : ConfirmError("Insufficient Fee")

    class SignFail(message: String) : ConfirmError(message)

    class BroadcastError(message: String) : ConfirmError(message)
}