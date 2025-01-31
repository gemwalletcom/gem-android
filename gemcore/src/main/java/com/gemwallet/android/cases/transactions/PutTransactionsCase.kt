package com.gemwallet.android.cases.transactions

import com.gemwallet.android.model.Transaction


interface PutTransactionsCase {
    suspend fun putTransactions(walletId: String, transactions: List<Transaction>)
}