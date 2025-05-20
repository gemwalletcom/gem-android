package com.gemwallet.android.data.repositoreis.session

import com.gemwallet.android.cases.session.GetCurrentCurrencyCase
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.flow.Flow

interface SessionRepository : GetCurrentCurrencyCase {

    fun session(): Flow<Session?>

    fun getSession(): Session?

    fun hasSession(): Boolean

    suspend fun setWallet(wallet: Wallet)

    suspend fun setCurrency(currency: Currency)

    suspend fun reset()
}