package com.gemwallet.android.features.asset.chart.models

import com.gemwallet.android.ui.models.PriceState

class PricePoint(
    val y: Float,
    val yLabel: String?,
    val timestamp: Long,
    val percentage: String,
    val priceState: PriceState,
)