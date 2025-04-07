package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class XrpTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) : TransactionStatusClient {
    override suspend fun getStatus(request: TransactionStateRequest): TransactionChages {
        val resp = rpcClient.transaction(request.hash).getOrNull() ?: throw ServiceUnavailable
        return TransactionChages(
            if (resp.result.status == "success") {
                TransactionState.Confirmed
            } else {
                TransactionState.Pending
            }
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}