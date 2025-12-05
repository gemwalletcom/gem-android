package com.gemwallet.android.application.perpetual.coordinators

import com.wallet.core.primitives.ChartCandleStick
import kotlinx.coroutines.flow.Flow

interface GetPerpetualCandleSticks {
    fun getPerpetualCandleSticks(perpetualId: String): Flow<List<ChartCandleStick>>
}