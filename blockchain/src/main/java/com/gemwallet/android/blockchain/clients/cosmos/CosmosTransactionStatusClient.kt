package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class CosmosTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: CosmosRpcClient,
) : TransactionStatusClient {
    override suspend fun getStatus(chain: Chain, owner: String, txId: String): Result<TransactionChages> {
        return rpcClient.transaction(txId).mapCatching {
            TransactionChages(
                if (it.tx_response == null || it.tx_response.txhash.isEmpty()) {
                    TransactionState.Pending
                } else if (it.tx_response.code == 0) {
                    TransactionState.Confirmed
                } else {
                    TransactionState.Reverted
                }
            )
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}