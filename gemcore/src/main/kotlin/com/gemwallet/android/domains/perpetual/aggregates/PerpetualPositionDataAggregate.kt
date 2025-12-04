package com.gemwallet.android.domains.perpetual.aggregates

import com.gemwallet.android.domains.price.PriceState
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.PerpetualDirection

interface PerpetualPositionDataAggregate {
    val positionId: String
    val perpetualId: String
    val asset: Asset
    val name: String
    val direction: PerpetualDirection
    val leverage: Int
    val marginAmount: String
    val pnlWithPercentage: String
    val pnlState: PriceState
}