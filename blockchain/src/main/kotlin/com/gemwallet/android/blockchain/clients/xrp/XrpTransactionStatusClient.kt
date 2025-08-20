package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class XrpTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) : TransactionStatusClient {
    override suspend fun getStatus(request: TransactionStateRequest): TransactionChanges {
        val resp = rpcClient.transaction(request.hash).getOrNull() ?: throw ServiceUnavailable
        return TransactionChanges(
            if (resp.result.status == "success") {
                TransactionState.Confirmed
            } else {
                TransactionState.Pending
            }
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}