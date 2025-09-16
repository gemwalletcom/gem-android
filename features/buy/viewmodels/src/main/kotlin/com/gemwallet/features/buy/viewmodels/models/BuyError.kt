package com.gemwallet.features.buy.viewmodels.models

sealed interface BuyError {
    data object EmptyAmount : BuyError

    data object MinimumAmount : BuyError

    data object QuoteNotAvailable : BuyError

    data object ValueIncorrect : BuyError
}