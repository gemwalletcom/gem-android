package com.gemwallet.android.features.confirm.models

sealed class ConfirmError(message: String) : Exception(message){

    object None : ConfirmError("")

    class Init(message: String) : ConfirmError(message)

    class PreloadError(message: String) : ConfirmError(message)

    object TransactionIncorrect : ConfirmError("Transaction data incorrect")

    object RecipientEmpty : ConfirmError("Recipient can't empty")

    class InsufficientBalance(val chainTitle: String) : ConfirmError("Insufficient Balance")

    class InsufficientFee(val chainTitle: String) : ConfirmError("Insufficient Fee")

    class SignFail(message: String) : ConfirmError(message)

    class BroadcastError(message: String) : ConfirmError(message)
}