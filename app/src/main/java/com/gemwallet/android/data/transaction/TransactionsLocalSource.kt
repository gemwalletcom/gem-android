package com.gemwallet.android.data.transaction

import com.wallet.core.primitives.Transaction
import com.wallet.core.primitives.TransactionExtended
import com.wallet.core.primitives.TransactionState
import kotlinx.coroutines.flow.Flow

interface TransactionsLocalSource {

    suspend fun putTransactions(transactions: List<Transaction>)

    suspend fun addTransaction(transaction: Transaction): Boolean

    suspend fun updateTransaction(txs: List<Transaction>): Boolean

    fun getExtendedTransactions(): Flow<List<TransactionExtended>>

    fun getExtendedTransaction(txId: String): Flow<TransactionExtended?>

    fun getTransactionsByState(state: TransactionState): Flow<List<TransactionExtended>>

    suspend fun getPending(): List<Transaction>

    suspend fun remove(tx: Transaction)

    suspend fun getMetadata(txId: String): Any?
}