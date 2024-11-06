package com.gemwallet.android.ui.models

import com.gemwallet.android.model.cryptoFormat
import com.gemwallet.android.model.format
import java.math.BigDecimal

interface CryptoFormattedUIModel : CryptoAmountUIModel, AssetUIModel {
    val cryptoFormatted: String
        get() = cryptoFormat(
            value = BigDecimal.valueOf(cryptoAmount),
            symbol = asset.symbol,
            decimalPlace = 6,
            dynamicPlace = true
        )
}