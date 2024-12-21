package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandTxStatusService
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class AlgorandTransactionStatusClient(
    private val chain: Chain,
    private val txStatusService: AlgorandTxStatusService,
) : TransactionStatusClient {

    override suspend fun getStatus(
        chain: Chain,
        owner: String,
        txId: String
    ): Result<TransactionChages> {
        val round = txStatusService.transaction(txId).getOrNull()?.confirmed_round
            ?: return Result.failure(Exception())
        return Result.success(
            TransactionChages(
                if (round > 0) TransactionState.Confirmed else TransactionState.Failed
            )
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}