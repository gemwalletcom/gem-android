package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class BitcoinTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: BitcoinRpcClient
) : TransactionStatusClient {

    override suspend fun getStatus(chain: Chain, owner: String, txId: String): Result<TransactionChages> {
        return rpcClient.transaction(txId).mapCatching {
            TransactionChages(
                if (it.blockHeight > 0) TransactionState.Confirmed else TransactionState.Pending
            )
        }
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain
}