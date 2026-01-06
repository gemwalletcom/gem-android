package com.gemwallet.android.application.transactions.coordinators

import com.gemwallet.android.domains.transaction.aggregates.TransactionDetailsAggregate
import kotlinx.coroutines.flow.Flow

interface GetTransactionDetails {
    fun getTransactionDetails(id: String): Flow<TransactionDetailsAggregate?>
}