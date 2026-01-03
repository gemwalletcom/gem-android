package com.gemwallet.android.application.transactions.coordinators

import com.gemwallet.android.model.TransactionExtended
import kotlinx.coroutines.flow.Flow

interface GetChangedTransactions {

    fun getChangedTransactions(): Flow<List<TransactionExtended>>
}