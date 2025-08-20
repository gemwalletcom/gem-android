package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandTxStatusService
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class AlgorandTransactionStatusClient(
    private val chain: Chain,
    private val txStatusService: AlgorandTxStatusService,
) : TransactionStatusClient {

    override suspend fun getStatus(request: TransactionStateRequest): TransactionChanges {
        val round = txStatusService.transaction(request.hash).getOrNull()?.confirmed_round ?: throw ServiceUnavailable
        return TransactionChanges(if (round > 0) TransactionState.Confirmed else TransactionState.Failed)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}