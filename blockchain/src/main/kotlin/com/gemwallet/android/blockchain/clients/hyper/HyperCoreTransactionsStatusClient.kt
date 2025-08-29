package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.Chain

class HyperCoreTransactionsStatusClient(
    private val chain: Chain,
) : TransactionStatusClient {
    override suspend fun getStatus(request: TransactionStateRequest): TransactionChanges {
        TODO("Not yet implemented")
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}