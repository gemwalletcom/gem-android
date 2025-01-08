package com.gemwallet.android.ui.models

import com.gemwallet.android.model.cryptoFormat
import java.math.BigDecimal

interface CryptoFormattedUIModel : CryptoAmountUIModel, AssetUIModel {
    val cryptoFormatted: String
        get() = cryptoFormat(
            value = BigDecimal.valueOf(cryptoAmount),
            symbol = asset.symbol,
            decimalPlace = fraction,
            dynamicPlace = true
        )

    val fraction: Int
}