package com.gemwallet.android.ui.components.list_item

import com.gemwallet.android.ui.models.PriceUIModel
import com.wallet.core.primitives.AssetPrice
import com.wallet.core.primitives.Currency

class AssetPriceUIModel(
    override val currency: Currency,
    private val assetPrice: AssetPrice?,
) : PriceUIModel {
    override val percentage: Double?
        get() = assetPrice?.priceChangePercentage24h

    override val fiat: Double?
        get() = assetPrice?.price
}