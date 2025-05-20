package com.gemwallet.android.features.asset.chart.models

import com.wallet.core.primitives.ChartPeriod

class ChartUIModel(
    val period: ChartPeriod = ChartPeriod.Day,
    val currentPoint: PricePoint? = null,
    val chartPoints: List<PricePoint> = emptyList(),
) {
    class State(
        val loading: Boolean = true,
        val period: ChartPeriod = ChartPeriod.Day,
        val empty: Boolean = false,
    )
}