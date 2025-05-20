package com.gemwallet.android.cases.transactions

import com.gemwallet.android.model.TransactionExtended
import kotlinx.coroutines.flow.Flow

interface GetTransaction {
    fun getTransaction(txId: String): Flow<TransactionExtended?>
}