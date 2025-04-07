package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinTransactionsService
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class BitcoinTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: BitcoinTransactionsService
) : TransactionStatusClient {

    override suspend fun getStatus(request: TransactionStateRequest): TransactionChages {
        val tx = rpcClient.transaction(request.hash).getOrNull() ?: throw ServiceUnavailable
        return TransactionChages(if (tx.blockHeight > 0) TransactionState.Confirmed else TransactionState.Pending)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}