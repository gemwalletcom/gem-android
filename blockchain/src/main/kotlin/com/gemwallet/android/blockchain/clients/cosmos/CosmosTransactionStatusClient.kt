package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosTransactionsService
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class CosmosTransactionStatusClient(
    private val chain: Chain,
    private val transactionsService: CosmosTransactionsService,
) : TransactionStatusClient {
    override suspend fun getStatus(request: TransactionStateRequest): TransactionChanges {
        val tx = transactionsService.transaction(request.hash).getOrNull() ?: throw ServiceUnavailable
        val state = when {
            tx.tx_response == null || tx.tx_response.txhash.isEmpty() -> TransactionState.Pending
            tx.tx_response.code == 0 -> TransactionState.Confirmed
            else -> TransactionState.Reverted
        }
        return TransactionChanges(state)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}