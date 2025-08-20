package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionNotFound
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class TonTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) : TransactionStatusClient {

    override suspend fun getStatus(request: TransactionStateRequest): TransactionChanges {
        val resp = rpcClient.transaction(request.hash).getOrNull() ?: throw ServiceUnavailable

        val transaction = resp.transactions.firstOrNull() ?: throw TransactionNotFound()
        val transactionState = when {
            transaction.out_msgs.firstOrNull()?.let { it.bounce && it.bounced } != false -> TransactionState.Failed
            else -> TransactionState.Confirmed
        }
        return TransactionChanges(state = transactionState)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}