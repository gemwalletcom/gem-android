package com.gemwallet.android.features.buy.models

sealed interface BuyError {
    data object MinimumAmount : BuyError

    data object QuoteNotAvailable : BuyError

    data object ValueIncorrect : BuyError
}