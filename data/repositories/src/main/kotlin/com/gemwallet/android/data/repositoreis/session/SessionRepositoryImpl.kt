package com.gemwallet.android.data.repositoreis.session

import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.data.service.store.database.SessionDao
import com.gemwallet.android.data.service.store.database.entities.DbSession
import com.gemwallet.android.data.service.store.database.entities.toModel
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Locale

class SessionRepositoryImpl(
    private val sessionDao: SessionDao,
    private val walletsRepository: WalletsRepository,
) : SessionRepository {

    override fun session(): Flow<Session?> = sessionDao.session().mapNotNull { record ->
        val wallet = record?.walletId?.let { walletsRepository.getWallet(it).firstOrNull() } ?: return@mapNotNull null
        record.toModel(wallet)
    }

//    override suspend fun getSession(): Session? {
//        val entity = sessionDao.getSession() ?: return null
//        val wallet = walletsRepository.getWallet(entity.walletId).firstOrNull() ?: return null
//        return entity.toModel(wallet)
//    }

//    override suspend fun hasSession(): Boolean = getSession() != null

    override suspend fun setWallet(wallet: Wallet) {
        val oldSession = runBlocking(Dispatchers.IO) { sessionDao.getSession() }
        val session = if (oldSession == null) {
            DbSession( // Create session
                walletId = wallet.id,
                currency = android.icu.util.Currency.getInstance(Locale.getDefault()).let { sysCurrency ->
                    Currency.entries.firstOrNull { it.string == sysCurrency.currencyCode } ?: Currency.USD
                }.string,
            )
        } else {
            oldSession.copy(walletId = wallet.id)
        }
        sessionDao.update(session)
    }

    override suspend fun setCurrency(currency: Currency) = withContext(Dispatchers.IO) {
        sessionDao.setCurrency(currency.string)
    }

    override suspend fun reset() = withContext(Dispatchers.IO) {
        sessionDao.clear()
    }

    override suspend fun getCurrentCurrency(): Currency = withContext(Dispatchers.IO) {
        val currency = sessionDao.getCurrency()
        val result = Currency.entries.firstOrNull { it.string == currency } ?: Currency.USD
        result
    }

    override fun getCurrency(): Flow<Currency> = session().map { it?.currency ?: Currency.USD }
}