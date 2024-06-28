package com.gemwallet.android.data.transaction

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.TransactionExtended
import kotlinx.coroutines.flow.Flow

interface TransactionsLocalSource {

    suspend fun putTransactions(transactions: List<Transaction>)

    suspend fun addTransaction(transaction: Transaction): Boolean

    suspend fun updateTransaction(txs: List<Transaction>): Boolean

    suspend fun getExtendedTransactions(txIds: List<String> = emptyList(), vararg accounts: Account): Flow<List<TransactionExtended>>

    suspend fun getExtendedTransactions(txIds: List<String> = emptyList()): Flow<List<TransactionExtended>>

    suspend fun getPending(): List<Transaction>

    suspend fun remove(tx: Transaction)

    suspend fun getMetadata(txId: String): Any?
}