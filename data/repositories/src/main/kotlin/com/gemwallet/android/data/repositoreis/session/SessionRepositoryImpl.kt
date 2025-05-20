package com.gemwallet.android.data.repositoreis.session

import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.data.service.store.database.SessionDao
import com.gemwallet.android.data.service.store.database.entities.toModel
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val sessionDao: SessionDao,
    private val walletsRepository: WalletsRepository,
) : SessionRepository {

//    private val sessionMapper = SessionMapper()

    override fun session(): Flow<Session?> = sessionDao.session().mapNotNull { record ->
        // TODO: dao map
        val wallet = record?.walletId?.let { walletsRepository.getWallet(it) } ?: return@mapNotNull null
        record.toModel(wallet)
    }

    override fun getSession(): Session? {
        val entity = runBlocking(Dispatchers.IO) { sessionDao.getSession() } ?: return null
        val wallet = runBlocking(Dispatchers.IO) {
            walletsRepository.getWallet(entity.walletId)
        } ?: return null
        return entity.toModel(wallet)
    }

    override fun hasSession(): Boolean = getSession() != null

    override suspend fun setWallet(wallet: Wallet)  = withContext(Dispatchers.IO) {
        val session = getSession()?.copy(wallet = wallet) ?: Session(
            wallet = wallet,
            currency = Currency.USD,
        )
        sessionDao.update(session.toRecord())
    }

    override suspend fun setCurrency(currency: Currency) = withContext(Dispatchers.IO) {
        val session = getSession() ?: return@withContext
        sessionDao.update(session.copy(currency = currency).toRecord())
    }

    override suspend fun reset() = withContext(Dispatchers.IO) {
        sessionDao.clear()
    }

    override fun getCurrentCurrency(): Currency = getSession()?.currency ?: Currency.USD

    override fun getCurrency(): Flow<Currency> = session().map { it?.currency ?: Currency.USD }
}