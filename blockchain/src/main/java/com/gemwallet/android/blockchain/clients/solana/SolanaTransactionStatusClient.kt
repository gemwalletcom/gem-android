package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
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

    override suspend fun getStatus(request: TransactionStateRequest): TransactionChages {
        val resp = transactionService.transaction(request.hash).getOrNull() ?: throw ServiceUnavailable
        if (resp.error != null) return TransactionChages(TransactionState.Failed)
        val state = if (resp.result.slot > 0) {
            if (resp.result.meta.err != null) {
                TransactionState.Failed
            } else {
                TransactionState.Confirmed
            }
        } else {
            TransactionState.Pending
        }
        return TransactionChages(state)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}