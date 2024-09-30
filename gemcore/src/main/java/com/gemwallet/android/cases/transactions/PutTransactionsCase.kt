package com.gemwallet.android.cases.transactions

import com.wallet.core.primitives.Transaction

interface PutTransactionsCase {
    suspend fun putTransactions(walletId: String, transactions: List<Transaction>)
}