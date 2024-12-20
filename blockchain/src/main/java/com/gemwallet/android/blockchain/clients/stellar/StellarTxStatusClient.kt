package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.stellar.services.StellarTxStatusService
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class StellarTxStatusClient(
    private val chain: Chain,
    private val txStatusService: StellarTxStatusService,
) : TransactionStatusClient {

    override suspend fun getStatus(
        chain: Chain,
        owner: String,
        txId: String
    ): Result<TransactionChages> {
        val tx = txStatusService.transaction().getOrNull() ?: return Result.failure(Exception())
        val state =
            if (tx.successful == true) TransactionState.Confirmed else TransactionState.Failed
        return Result.success(
            TransactionChages(
                state = state,
                fee = tx.fee_charged.toBigInteger()
            )
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}