package com.gemwallet.android.ui.models

import com.gemwallet.android.model.format
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Currency

interface FiatFormatterUIModel : CryptoAmountUIModel, AssetUIModel {
    val currency: Currency

    val priceValue: Double?

    val fiatAmountFormatted: String
        get() {
            val price = priceValue ?: return ""
            val fiat = crypto.convert(asset.decimals, price)
            return currency.format(fiat, decimalPlace = 2, dynamicPlace = true)
        }
}