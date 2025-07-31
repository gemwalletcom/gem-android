package com.gemwallet.features.transfer_amount.viewmodels.models

sealed interface AmountError {
    data object None : AmountError

    data object Required : AmountError

    data object Unavailable : AmountError

    data object IncorrectAmount : AmountError

    data object ZeroAmount : AmountError

    class InsufficientBalance(val assetName: String) : AmountError

    class InsufficientFeeBalance(val assetName: String) : AmountError

    class MinimumValue(val minimumValue: String) : AmountError

    data object IncorrectAddress : AmountError
}