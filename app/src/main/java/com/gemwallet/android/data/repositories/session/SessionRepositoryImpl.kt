package com.gemwallet.android.data.repositories.session

import com.gemwallet.android.data.database.SessionDao
import com.gemwallet.android.data.database.mappers.SessionMapper
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SessionRepositoryImpl(
    private val sessionDao: SessionDao,
    private val walletsRepository: WalletsRepository,
) : SessionRepository {

    override fun session(): Flow<Session?> = sessionDao.session().mapNotNull { entity ->
        val wallet = walletsRepository
            .getWallet(entity?.walletId ?: return@mapNotNull null).getOrNull() ?: return@mapNotNull null
        SessionMapper(wallet).asDomain(entity)
    }

    override fun getSession(): Session? {
        val entity = runBlocking(Dispatchers.IO) { sessionDao.getSession() } ?: return null
        val wallet = runBlocking(Dispatchers.IO) {
            walletsRepository.getWallet(entity.walletId).getOrNull()
        } ?: return null
        return SessionMapper(wallet).asDomain(entity)
    }

    override fun hasSession(): Boolean = getSession() != null

    override fun subscribe(onSessionChange: (Session) -> Unit) { }

    override fun subscribe(onSessionChange: OnSessionChange) { }

    override suspend fun setWallet(wallet: Wallet)  = withContext(Dispatchers.IO) {
        val session = getSession() ?: return@withContext
        sessionDao.update(SessionMapper(wallet).asEntity(session.copy(wallet = wallet)))
    }

    override suspend fun setCurrency(currency: Currency) = withContext(Dispatchers.IO) {
        val session = getSession() ?: return@withContext
        sessionDao.update(SessionMapper(session.wallet).asEntity(session.copy(currency = currency)))
    }

    override suspend fun reset() = withContext(Dispatchers.IO) {
        sessionDao.clear()
    }
}