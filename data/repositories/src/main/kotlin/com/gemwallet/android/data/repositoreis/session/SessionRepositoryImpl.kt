package com.gemwallet.android.data.repositoreis.session

import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.data.service.store.database.SessionDao
import com.gemwallet.android.data.service.store.database.entities.DbSession
import com.gemwallet.android.data.service.store.database.entities.toModel
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SessionRepositoryImpl(
    private val sessionDao: SessionDao,
    private val walletsRepository: WalletsRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : SessionRepository {

    val session = sessionDao.session().flatMapLatest { record ->
        val walletId = record?.walletId ?: return@flatMapLatest flow { emit(null) }
        walletsRepository.getWallet(walletId).mapLatest { wallet ->
            val session = record.toModel(wallet ?: return@mapLatest null)
            session
        }
    }
    .stateIn(scope, SharingStarted.Eagerly, null)

    override fun session(): StateFlow<Session?> = session

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