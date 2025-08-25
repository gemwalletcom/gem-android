package com.gemwallet.android.cases.transactions

import com.gemwallet.android.model.TransactionExtended
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionState
import kotlinx.coroutines.flow.Flow

interface GetTransactions {
    fun getTransactions(
        assetId: AssetId? = null,
        state: TransactionState? = null
    ): Flow<List<TransactionExtended>>

    fun getChangedTransactions(): Flow<List<TransactionExtended>>

    fun getPendingTransactionsCount(): Flow<Int?>
}