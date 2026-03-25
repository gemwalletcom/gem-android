package com.gemwallet.android.domains.perpetual.aggregates

import com.gemwallet.android.domains.price.values.EquivalentValue
import com.wallet.core.primitives.Asset

interface PerpetualDataAggregate {

    val id: String

    val name: String

    val icon: Any

    val price: EquivalentValue

    val volume: String

    val asset: Asset

    val isPinned: Boolean
}