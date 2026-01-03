package com.gemwallet.android.data.coordinates.transaction

import com.gemwallet.android.application.transactions.coordinators.GetTransactions
import com.gemwallet.android.data.repositoreis.transactions.TransactionRepository
import com.gemwallet.android.domains.transaction.aggregates.TransactionDataAggregate
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.model.TransactionExtended
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.TransactionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class GetTransactionsImpl(
    private val transactionsRepository: TransactionRepository,
) : GetTransactions {

    override fun getTransactions(
        assetId: AssetId?,
        state: TransactionState?
    ): Flow<List<TransactionExtended>> {
        return transactionsRepository.getTransactions()
            .map { txs -> txs.filter { state == null || it.transaction.state == state } }
            .map { items ->
                items.filter {
                    val swapMetadata = it.transaction.getSwapMetadata()
                    assetId == null || it.asset.id == assetId
                            || swapMetadata?.toAsset == assetId
                            || swapMetadata?.fromAsset == assetId
                }
            }
            .flowOn(Dispatchers.IO)
    }

    override fun getTransactions(
        assetId: AssetId?,
        state: TransactionState?,
        flag: Int
    ): Flow<List<TransactionDataAggregate>> {
        TODO("Not yet implemented")
    }
}