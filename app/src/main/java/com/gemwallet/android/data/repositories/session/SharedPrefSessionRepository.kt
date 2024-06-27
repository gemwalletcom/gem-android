package com.gemwallet.android.data.repositories.session

import android.util.Log
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefSessionRepository @Inject constructor(
    private val sessionLocalSource: SessionLocalSource,
    private val walletsRepository: WalletsRepository,
) : SessionRepository {
    private val _session = MutableStateFlow<Session?>(null)

    private val state: StateFlow<Session?> = _session.asStateFlow()

    private val subscribers = CopyOnWriteArrayList<WeakReference<OnSessionChange>>()

    init {
        runBlocking {
            restoreSession()
        }
    }

    override fun session(): StateFlow<Session?> = state

    override fun getSession(): Session? = _session.value

    override fun hasSession(): Boolean = sessionLocalSource.getWalletId() != null

    override fun subscribe(onSessionChange: (Session) -> Unit) {
        onSessionChange(runBlocking { getSession() } ?: return)
    }

    override fun subscribe(onSessionChange: OnSessionChange) {
        onSessionChange.onSessionChange(runBlocking { getSession() } ?: return)
        subscribers.add(WeakReference(onSessionChange))
    }

    override suspend fun setWallet(wallet: Wallet) {
        sessionLocalSource.setWallet(wallet.id)
        _session.update { Session(wallet, it?.currency ?: Currency.USD) }
        notifySubscribers()
    }

    override suspend fun setCurrency(currency: Currency) {
        sessionLocalSource.setCurrency(currency.string)
        _session.update { Session(it!!.wallet, currency) }
        notifySubscribers()
    }

    override suspend fun reset() {
        sessionLocalSource.reset()
        _session.update { null }
    }

    private fun notifySubscribers() {
        val live = subscribers.filter { it.get() != null }
        live.forEach {
            it.get()?.onSessionChange(runBlocking { getSession() } ?: return)
        }
        subscribers.clear()
        subscribers.addAll(live)
    }

    private suspend fun restoreSession() = withContext(Dispatchers.IO) {
        val walletId = sessionLocalSource.getWalletId() ?: return@withContext
        walletsRepository.getWallet(walletId)
            .onSuccess { wallet ->
                _session.update { Session(wallet = wallet, currency = sessionLocalSource.getCurrency()) }
            }
            .onFailure {
                Log.d("SESSION_RESTORE", "Restore session error: ", it)
            }

    }
}