package com.gemwallet.android.domains.pricealerts.aggregates

import com.gemwallet.android.domains.price.PriceState
import com.wallet.core.primitives.AssetId

interface PriceAlertDataAggregate {
    val id: Int
    val assetId: AssetId
    val icon: Any
    val title: String
    val titleBadge: String
    val priceState: PriceState
    val price: String
    val percentage: String
    val type: PriceAlertType
    val hasTarget: Boolean
}

enum class PriceAlertType {
    Auto,
    Over,
    Under,
    Increase,
    Decrease,
}