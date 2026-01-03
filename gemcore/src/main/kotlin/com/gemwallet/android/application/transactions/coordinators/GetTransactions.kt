package com.gemwallet.android.application.transactions.coordinators

import com.gemwallet.android.domains.transaction.aggregates.TransactionDataAggregate
import com.gemwallet.android.model.TransactionExtended
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionState
import kotlinx.coroutines.flow.Flow

interface GetTransactions {

    @Deprecated("Remove when all will move to getTransactions(...): Flow<List<TransactionDataAggregate>>")
    fun getTransactions(
        assetId: AssetId? = null,
        state: TransactionState? = null,
    ): Flow<List<TransactionExtended>>

    fun getTransactions(
        assetId: AssetId? = null,
        state: TransactionState? = null,
        flag: Int = 0,
    ): Flow<List<TransactionDataAggregate>>
}