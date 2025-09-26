package com.gemwallet.features.confirm.models

import com.wallet.core.primitives.Chain

sealed class ConfirmError(message: String) : Exception(message){

    object None : ConfirmError("")

    class Init(message: String) : ConfirmError(message)

    class PreloadError(message: String) : ConfirmError(message)

    object TransactionIncorrect : ConfirmError("Transaction data incorrect")

    object RecipientEmpty : ConfirmError("Recipient can't empty")

    class InsufficientBalance(val chainTitle: String) : ConfirmError("Insufficient Balance")

    class InsufficientFee(val chain: Chain) : ConfirmError("Insufficient Fee")

    class SignFail(message: String) : ConfirmError(message)

    class BroadcastError(message: String) : ConfirmError(message)
}