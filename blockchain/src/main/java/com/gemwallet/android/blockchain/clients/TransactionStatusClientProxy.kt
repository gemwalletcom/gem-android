package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain

class TransactionStatusClientProxy(
    private val clients: List<TransactionStatusClient>,
) : TransactionStatusClient {
    override suspend fun getStatus(
        chain: Chain,
        owner: String,
        txId: String
    ): Result<TransactionChages> {
        return clients.getClient(chain)?.getStatus(chain, owner, txId) ?: Result.failure(Exception("Chain isn't support"))
    }

    override fun isMaintain(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}