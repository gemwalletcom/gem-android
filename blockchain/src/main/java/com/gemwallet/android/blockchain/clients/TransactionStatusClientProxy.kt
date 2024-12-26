package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain

class TransactionStatusClientProxy(
    private val clients: List<TransactionStatusClient>,
) : TransactionStatusClient {
    override suspend fun getStatus(request: TransactionStateRequest): Result<TransactionChages> {
        return clients.getClient(request.chain)?.getStatus(request) ?: Result.failure(Exception("Chain isn't support"))
    }

    override fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}