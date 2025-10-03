package com.gemwallet.android.ui.models

interface CryptoAmountUIModel : UIModel {
    val cryptoAmount: Double

    val isZeroAmount: Boolean
        get() = cryptoAmount == 0.0
}