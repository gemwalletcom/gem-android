package com.gemwallet.android.cases.session

import com.wallet.core.primitives.Currency
import kotlinx.coroutines.flow.Flow

interface GetCurrentCurrencyCase {
    suspend fun getCurrentCurrency(): Currency

    fun getCurrency(): Flow<Currency>
}