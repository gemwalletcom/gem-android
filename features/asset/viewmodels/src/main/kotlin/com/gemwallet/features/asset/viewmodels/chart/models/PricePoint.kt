package com.gemwallet.features.asset.viewmodels.chart.models

import com.gemwallet.android.domains.price.PriceState

class PricePoint(
    val y: Float,
    val yLabel: String?,
    val timestamp: Long,
    val percentage: String,
    val priceState: PriceState,
)