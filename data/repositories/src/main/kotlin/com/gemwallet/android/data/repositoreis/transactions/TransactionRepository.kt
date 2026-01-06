package com.gemwallet.android.data.repositoreis.transactions

import com.gemwallet.android.model.TransactionExtended
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(): Flow<List<TransactionExtended>>

    fun getTransaction(id: String): Flow<TransactionExtended?>
}