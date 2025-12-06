package com.gemwallet.android.data.repositoreis.perpetual

import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.PerpetualBalance
import com.wallet.core.primitives.PerpetualData
import com.wallet.core.primitives.PerpetualPosition
import com.wallet.core.primitives.PerpetualPositionData
import kotlinx.coroutines.flow.Flow

class PerpetualRepositoryImpl(
) : PerpetualRepository {

    override fun putPerpetuals(items: List<PerpetualData>) {
        TODO("Not yet implemented")
    }

    override fun getPerpetuals(query: String?): Flow<List<PerpetualData>> {
        TODO("Not yet implemented")
    }

    override fun getPerpetual(perpetualId: String): Flow<PerpetualData> {
        TODO("Not yet implemented")
    }

    override suspend fun putPerpetualChartData(data: List<ChartCandleStick>) {
        TODO("Not yet implemented")
    }

    override fun getPerpetualChartData(perpetualId: String): Flow<List<ChartCandleStick>> {
        TODO("Not yet implemented")
    }

    override suspend fun putPositions(items: List<PerpetualPosition>) {
        TODO("Not yet implemented")
    }

    override fun getPositions(): Flow<List<PerpetualPositionData>> {
        TODO("Not yet implemented")
    }

    override fun getPosition(positionId: String): Flow<PerpetualPositionData> {
        TODO("Not yet implemented")
    }

    override suspend fun putBalance(balance: PerpetualBalance) {
        TODO("Not yet implemented")
    }

    override fun getBalances(): Flow<List<PerpetualBalance>> {
        TODO("Not yet implemented")
    }
}