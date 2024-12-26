package com.gemwallet.android.blockchain.clients.bitcoin

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

    override suspend fun getStatus(request: TransactionStateRequest): Result<TransactionChages> {
        return rpcClient.transaction(request.hash).mapCatching {
            TransactionChages(
                if (it.blockHeight > 0) TransactionState.Confirmed else TransactionState.Pending
            )
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}