package com.gemwallet.android.cases.transactions

import com.wallet.core.primitives.TransactionExtended
import kotlinx.coroutines.flow.Flow

interface GetTransactionCase {
    fun getTransaction(txId: String): Flow<TransactionExtended?>
}