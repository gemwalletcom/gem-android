package com.gemwallet.android.domains.asset.aggregates

import com.gemwallet.android.domains.price.values.PriceableValue
import com.wallet.core.primitives.AssetId

interface AssetInfoDataAggregate {
    val id: AssetId
    val title: String
    val icon: Any?
    val balance: String
    val balanceEquivalent: String
    val isZeroBalance: Boolean
    val price: PriceableValue?
    val position: Int
    val pinned: Boolean
    val accountAddress: String
}