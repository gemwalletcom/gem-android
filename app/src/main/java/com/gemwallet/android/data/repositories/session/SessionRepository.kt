package com.gemwallet.android.data.repositories.session

import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository {

    fun session(): Flow<Session?>

    fun getSession(): Session?

    fun hasSession(): Boolean

    fun subscribe(onSessionChange: (Session) -> Unit)

    fun subscribe(onSessionChange: OnSessionChange)

    suspend fun setWallet(wallet: Wallet)

    suspend fun setCurrency(currency: Currency)

    suspend fun reset()
}