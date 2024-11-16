package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class XrpTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) : TransactionStatusClient {
    override suspend fun getStatus(chain: Chain, owner: String, txId: String): Result<TransactionChages> {
        return rpcClient.transaction(txId).mapCatching {
            TransactionChages(
                if (it.result.status == "success") TransactionState.Confirmed else TransactionState.Pending
            )
        }
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain
}