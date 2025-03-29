package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class SuiTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient,
) : TransactionStatusClient {
    override suspend fun getStatus(request: TransactionStateRequest): TransactionChages {
        val resp = rpcClient.transaction(request.hash).getOrNull() ?: throw ServiceUnavailable
        return TransactionChages(
            when (resp.result.effects.status.status) {
                "success" -> TransactionState.Confirmed
                "failure" -> TransactionState.Reverted
                else -> TransactionState.Pending
            }
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}