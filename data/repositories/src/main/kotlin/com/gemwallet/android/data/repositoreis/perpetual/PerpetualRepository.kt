package com.gemwallet.android.data.repositoreis.perpetual

import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.PerpetualData
import com.wallet.core.primitives.PerpetualPositionData
import kotlinx.coroutines.flow.Flow

interface PerpetualRepository {
    fun putPerpetuals(items: List<PerpetualData>)

    fun getPerpetuals(query: String? = null): Flow<List<PerpetualData>>

    fun getPerpetual(perpetualId: String): Flow<PerpetualData>

    fun putPerpetualChartData(data: List<ChartCandleStick>)

    fun getPerpetualChartData(perpetualId: String): Flow<List<ChartCandleStick>>

    fun putPositions(items: List<PerpetualPositionData>)

    fun getPositions(): Flow<List<PerpetualPositionData>>

    fun getPosition(positionId: String): Flow<PerpetualPositionData>
}