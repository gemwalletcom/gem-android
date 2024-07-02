package com.gemwallet.android.features.asset.chart.models

import com.gemwallet.android.features.assets.model.PriceState

class PricePoint(
    val y: Float,
    val yLabel: String?,
    val timestamp: Long,
    val percentage: String,
    val priceState: PriceState,
)