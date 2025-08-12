package com.gemwallet.features.transfer_amount.viewmodels.models

sealed class AmountError : Exception() {
    object None : AmountError()

    object Required : AmountError()

    object Unavailable : AmountError()

    object IncorrectAmount : AmountError()

    object ZeroAmount : AmountError()

    class InsufficientBalance(val assetName: String) : AmountError()

    class InsufficientFeeBalance(val assetName: String) : AmountError()

    class MinimumValue(val minimumValue: String) : AmountError()

    object IncorrectAddress : AmountError()

    class Unknown(val data: String) : AmountError()
}