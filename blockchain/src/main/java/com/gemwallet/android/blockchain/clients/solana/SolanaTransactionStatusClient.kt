package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.solana.services.SolanaTransactionsService
import com.gemwallet.android.blockchain.clients.solana.services.transaction
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class SolanaTransactionStatusClient(
    private val chain: Chain,
    private val transactionService: SolanaTransactionsService,
) : TransactionStatusClient {

    override suspend fun getStatus(request: TransactionStateRequest): Result<TransactionChages> {

        return transactionService.transaction(request.hash).mapCatching {
            if (it.error != null) return@mapCatching TransactionChages(TransactionState.Failed)
            val state = if (it.result.slot > 0) {
                if (it.result.meta.err != null) {
                    TransactionState.Failed
                } else {
                    TransactionState.Confirmed
                }
            } else {
                TransactionState.Pending
            }
            TransactionChages(state)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}