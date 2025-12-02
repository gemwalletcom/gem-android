package com.gemwallet.android.application.perpetual.coordinators

import com.wallet.core.primitives.ChartCandleStick
import kotlinx.coroutines.flow.Flow

interface GetPerpetualChartData {
    fun getPerpetualChartData(perpetualId: String): Flow<ChartCandleStick>
}