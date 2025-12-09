package com.gemwallet.android.application.perpetual.coordinators

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.ChartPeriod
import kotlinx.coroutines.flow.Flow

interface GetPerpetualChartData {
    suspend fun getPerpetualChartData(chain: Chain, symbol: String, period: ChartPeriod): List<ChartCandleStick>
}