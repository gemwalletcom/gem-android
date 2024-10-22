package com.gemwallet.android.ui.models

import com.gemwallet.android.model.format

interface CryptoFormattedUIModel : CryptoAmountUIModel, AssetUIModel {

    val cryptoFormatted: String
        get() = asset.format(crypto = crypto, decimalPlace = 6, dynamicPlace = true)
}