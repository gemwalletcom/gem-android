package com.gemwallet.android.data.repositoreis.perpetual

import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.PerpetualBalance
import com.wallet.core.primitives.PerpetualData
import com.wallet.core.primitives.PerpetualPosition
import com.wallet.core.primitives.PerpetualPositionData
import kotlinx.coroutines.flow.Flow

interface PerpetualRepository {
    fun putPerpetuals(items: List<PerpetualData>)

    fun getPerpetuals(query: String? = null): Flow<List<PerpetualData>>

    fun getPerpetual(perpetualId: String): Flow<PerpetualData>

    suspend fun putPerpetualChartData(data: List<ChartCandleStick>)

    fun getPerpetualChartData(perpetualId: String): Flow<List<ChartCandleStick>>

    suspend fun putPositions(items: List<PerpetualPosition>)

    fun getPositions(): Flow<List<PerpetualPositionData>>

    fun getPosition(positionId: String): Flow<PerpetualPositionData>

    suspend fun putBalance(balance: PerpetualBalance)

    fun getBalances(): Flow<List<PerpetualBalance>>
}