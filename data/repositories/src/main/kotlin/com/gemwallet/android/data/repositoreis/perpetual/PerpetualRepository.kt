package com.gemwallet.android.data.repositoreis.perpetual

import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.PerpetualBalance
import com.wallet.core.primitives.PerpetualData
import com.wallet.core.primitives.PerpetualMetadata
import com.wallet.core.primitives.PerpetualPosition
import com.wallet.core.primitives.PerpetualPositionData
import kotlinx.coroutines.flow.Flow

// TODO: It isn't correct package
interface PerpetualRepository {
    suspend fun putPerpetuals(items: List<PerpetualData>)

    suspend fun removeNotAvailablePerpetuals(items: List<PerpetualData>)

    fun getPerpetuals(query: String? = null): Flow<List<PerpetualData>>

    fun getPerpetual(perpetualId: String): Flow<PerpetualData?>

    suspend fun putPerpetualChartData(data: List<ChartCandleStick>)

    fun getPerpetualChartData(perpetualId: String): Flow<List<ChartCandleStick>>

    suspend fun removeNotAvailablePositions(accountAddress: String, items: List<PerpetualPosition>)

    suspend fun putPositions(accountAddress: String, items: List<PerpetualPosition>)

    fun getPositions(accountAddress: List<String>): Flow<List<PerpetualPositionData>>

    fun getPositionByPositionId(id: String): Flow<PerpetualPositionData?>

    fun getPositionByPerpetualId(id: String): Flow<PerpetualPositionData?>

    suspend fun putBalance(accountAddress: String, balance: PerpetualBalance)

    fun getBalance(accountAddress: String): Flow<PerpetualBalance?>

    fun getBalances(accountAddresses: List<String>): Flow<List<PerpetualBalance>>

    suspend fun setMetadata(perpetualId: String, metadata: PerpetualMetadata)
}