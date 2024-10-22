package com.gemwallet.android.ui.models

import com.gemwallet.android.model.Fiat
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Currency

class AssetPriceUIModel(
    override val currency: Currency,
    private val assetPrice: AssetPrice?,
) : PriceUIModel {
    override val dayChange: Double?
        get() = assetPrice?.priceChangePercentage24h

    override val fiat: Fiat?
        get() = assetPrice?.price?.let { Fiat(it) }
}