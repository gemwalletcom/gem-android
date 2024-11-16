package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class SuiTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient,
) : TransactionStatusClient {
    override suspend fun getStatus(chain: Chain, owner: String, txId: String): Result<TransactionChages> {
        return rpcClient.transaction(txId).mapCatching {
            TransactionChages(
                when (it.result.effects.status.status) {
                    "success" -> TransactionState.Confirmed
                    "failure" -> TransactionState.Reverted
                    else -> TransactionState.Pending
                }
            )
        }
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain
}