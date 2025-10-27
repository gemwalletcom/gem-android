package com.gemwallet.android.data.repositoreis.session

import com.gemwallet.android.cases.session.GetCurrentCurrencyCase
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.flow.StateFlow

interface SessionRepository : GetCurrentCurrencyCase {

    fun session(): StateFlow<Session?>

    suspend fun setWallet(wallet: Wallet)

    suspend fun setCurrency(currency: Currency)

    suspend fun reset()
}