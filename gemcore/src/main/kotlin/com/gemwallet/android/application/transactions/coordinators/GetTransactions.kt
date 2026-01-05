package com.gemwallet.android.application.transactions.coordinators

import com.gemwallet.android.domains.transaction.aggregates.TransactionDataAggregate
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import com.wallet.core.primitives.TransactionType
import kotlinx.coroutines.flow.Flow

interface GetTransactions {
    fun getTransactions(
        assetId: AssetId? = null,
        state: TransactionState? = null,
        filterByChains: List<Chain> = emptyList(), // TODO: Improve filters
        filterByType: List<TransactionType> = emptyList(), // TODO: Improve filters
    ): Flow<List<TransactionDataAggregate>>
}