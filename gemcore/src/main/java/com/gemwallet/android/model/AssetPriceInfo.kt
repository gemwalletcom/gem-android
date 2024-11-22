package com.gemwallet.android.model

import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Currency

class AssetPriceInfo(
    val currency: Currency,
    val price: AssetPrice,
) {
    override fun equals(other: Any?): Boolean {
        return other is AssetPriceInfo
                && currency == other.currency
                && price.assetId == other.price.assetId
                && price.price == other.price.price
                && price.priceChangePercentage24h == other.price.priceChangePercentage24h
    }

    override fun hashCode(): Int {
        var result = currency.hashCode()
        result = 31 * result + price.hashCode()
        return result
    }
}