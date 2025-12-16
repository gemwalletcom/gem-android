package com.gemwallet.android.domains.perpetual.aggregates

import com.wallet.core.primitives.Asset

interface PerpetualDetailsDataAggregate {
    val id: String
    val asset: Asset
    val name: String
    val dayVolume: String
    val openInterest: String
    val funding: String
    val maxLeverage: Int
}