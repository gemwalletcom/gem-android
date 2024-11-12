package com.gemwallet.android.data.repositoreis.session

import com.gemwallet.android.data.service.store.database.SessionDao
import com.gemwallet.android.data.service.store.database.mappers.SessionMapper
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
    private val walletsRepository: com.gemwallet.android.data.repositoreis.wallets.WalletsRepository,
) : SessionRepository {

    private val sessionMapper = SessionMapper()

    override fun session(): Flow<Session?> = sessionDao.session().mapNotNull { entity ->
        val wallet = walletsRepository.getWallet(
            entity?.walletId ?: return@mapNotNull null
        ) ?: return@mapNotNull null
        sessionMapper.asDomain(entity, { wallet })
    }

    override fun getSession(): Session? {
        val entity = runBlocking(Dispatchers.IO) { sessionDao.getSession() } ?: return null
        val wallet = runBlocking(Dispatchers.IO) {
            walletsRepository.getWallet(entity.walletId)
        } ?: return null
        return sessionMapper.asDomain(entity, { wallet })
    }

    override fun hasSession(): Boolean = getSession() != null

    override suspend fun setWallet(wallet: Wallet)  = withContext(Dispatchers.IO) {
        val session = getSession()?.copy(wallet = wallet) ?: Session(
            wallet = wallet,
            currency = Currency.USD,
        )
        sessionDao.update(sessionMapper.asEntity(session))
    }

    override suspend fun setCurrency(currency: Currency) = withContext(Dispatchers.IO) {
        val session = getSession() ?: return@withContext
        sessionDao.update(sessionMapper.asEntity((session.copy(currency = currency))))
    }

    override suspend fun reset() = withContext(Dispatchers.IO) {
        sessionDao.clear()
    }

    override fun getCurrentCurrency(): Currency = getSession()?.currency ?: Currency.USD

    override fun getCurrency(): Flow<Currency> = session().map { it?.currency ?: Currency.USD }
}