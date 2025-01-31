package com.gemwallet.android.cases.transactions

import com.gemwallet.android.model.TransactionExtended
import kotlinx.coroutines.flow.Flow

interface GetTransactionCase {
    fun getTransaction(txId: String): Flow<TransactionExtended?>
}