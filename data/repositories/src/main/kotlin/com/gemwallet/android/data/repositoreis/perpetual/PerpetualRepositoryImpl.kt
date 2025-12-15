package com.gemwallet.android.data.repositoreis.perpetual

import com.gemwallet.android.data.service.store.database.PerpetualBalanceDao
import com.gemwallet.android.data.service.store.database.PerpetualDao
import com.gemwallet.android.data.service.store.database.PerpetualPositionDao
import com.gemwallet.android.data.service.store.database.entities.toDB
import com.gemwallet.android.data.service.store.database.entities.toDTO
import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.PerpetualBalance
import com.wallet.core.primitives.PerpetualData
import com.wallet.core.primitives.PerpetualMetadata
import com.wallet.core.primitives.PerpetualPosition
import com.wallet.core.primitives.PerpetualPositionData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PerpetualRepositoryImpl(
    private val perpetualDao: PerpetualDao,
    private val perpetualBalanceDao: PerpetualBalanceDao,
    private val perpetualPositionDao: PerpetualPositionDao,
) : PerpetualRepository {

    override suspend fun putPerpetuals(items: List<PerpetualData>) {
        perpetualDao.putPerpetualsData(items.map { it.perpetual.toDB() }, items.map { it.asset.toDB() })

    }

    override fun getPerpetuals(query: String?): Flow<List<PerpetualData>> {
        return perpetualDao.getPerpetualsData()
            .map { items -> items.mapNotNull { it.toDTO() } }
//            .map { items -> items.sortedByDescending { it.perpetual.funding } }
    }

    override fun getPerpetual(perpetualId: String): Flow<PerpetualData?> {
        return perpetualDao.getPerpetual(perpetualId)
            .map { it?.toDTO() }
    }

    override suspend fun putPerpetualChartData(data: List<ChartCandleStick>) {
        TODO("Not yet implemented")
    }

    override fun getPerpetualChartData(perpetualId: String): Flow<List<ChartCandleStick>> {
        TODO("Not yet implemented")
    }

    override suspend fun removeNotAvailablePositions(
        accountAddress: String,
        items: List<PerpetualPosition>
    ) {
        perpetualPositionDao.removeNotAvailablePositions(accountAddress, items.map { it.id })
    }

    override suspend fun putPositions(accountAddress: String, items: List<PerpetualPosition>) {
        perpetualPositionDao.putPositions(items.map { it.toDB(accountAddress) })
    }

    override fun getPositions(accountAddress: List<String>): Flow<List<PerpetualPositionData>> {
        return perpetualPositionDao.getPositionsData(accountAddress)
            .map { items -> items.mapNotNull { it.toDTO() } }
    }

    override fun getPositionByPositionId(id: String): Flow<PerpetualPositionData?> {
        return perpetualPositionDao.getPositionData(id).map { it?.toDTO() }
    }

    override fun getPositionByPerpetualId(id: String): Flow<PerpetualPositionData?> {
        return perpetualPositionDao.getPositionDataByPerpetual(id).map { it?.toDTO() }
    }

    override suspend fun putBalance(accountAddress: String, balance: PerpetualBalance) {
        perpetualBalanceDao.put(balance.toDB(accountAddress))
    }

    override fun getBalances(accountAddresses: List<String>): Flow<List<PerpetualBalance>> {
        return perpetualBalanceDao.getBalances(accountAddresses)
            .map { items -> items.map { it.toDTO() } }
    }

    override suspend fun setMetadata(perpetualId: String, metadata: PerpetualMetadata) {
        perpetualDao.setMetadata(metadata.toDB(perpetualId))
    }
}