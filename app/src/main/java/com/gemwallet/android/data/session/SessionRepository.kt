package com.gemwallet.android.data.session

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
class SessionRepository @Inject constructor(
    private val sessionLocalSource: SessionLocalSource,
    private val walletsRepository: WalletsRepository,
) {
    private val _session = MutableStateFlow<Session?>(null)

    val state: StateFlow<Session?> = _session.asStateFlow()

    val session: Session?
        get() {
            return _session.value
        }

    private val subscribers = CopyOnWriteArrayList<WeakReference<OnSessionChange>>()

    init {
        runBlocking {
            restoreSession()
        }
    }

    fun hasSession(): Boolean = sessionLocalSource.getWalletId() != null

    fun subscribe(onSessionChange: (Session) -> Unit) {
        onSessionChange(session ?: return)
    }

    fun subscribe(onSessionChange: OnSessionChange) {
        onSessionChange.onSessionChange(session ?: return)
        subscribers.add(WeakReference(onSessionChange))
    }

    fun setWallet(wallet: Wallet) {
        sessionLocalSource.setWallet(wallet.id)
        _session.update { Session(wallet, it?.currency ?: Currency.USD) }
        notifySubscribers()
    }

    fun setCurrency(currency: Currency) {
        sessionLocalSource.setCurrency(currency.string)
        _session.update { Session(it!!.wallet, currency) }
        notifySubscribers()
    }

    fun reset() {
        sessionLocalSource.reset()
        _session.update { null }
    }

    private fun notifySubscribers() {
        val live = subscribers.filter { it.get() != null }
        live.forEach {
            it.get()?.onSessionChange(session ?: return)
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