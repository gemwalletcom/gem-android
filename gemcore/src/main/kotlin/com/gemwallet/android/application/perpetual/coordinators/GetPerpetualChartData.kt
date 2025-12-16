package com.gemwallet.android.application.perpetual.coordinators

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.ChartPeriod

interface GetPerpetualChartData {
    suspend fun getPerpetualChartData(assetId: AssetId, period: ChartPeriod): List<ChartCandleStick>
}