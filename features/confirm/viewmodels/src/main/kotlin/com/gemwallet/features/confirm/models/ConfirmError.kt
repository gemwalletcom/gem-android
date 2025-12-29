package com.gemwallet.features.confirm.models

import com.wallet.core.primitives.Chain

sealed class ConfirmError : Exception() {

    object None : ConfirmError()

    object Init : ConfirmError()

    object PreloadError : ConfirmError()

    object TransactionIncorrect : ConfirmError()

    object RecipientEmpty : ConfirmError()

    class InsufficientBalance(val chainTitle: String) : ConfirmError()

    class InsufficientFee(val chain: Chain) : ConfirmError()

    object SignFail : ConfirmError()

    class BroadcastError(val details: String) : ConfirmError()

    class DustThreshold(val chainTitle: String) : ConfirmError()
}