package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.clients.stellar.services.StellarTxStatusService
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class StellarTransactionStatusClient(
    private val chain: Chain,
    private val txStatusService: StellarTxStatusService,
) : TransactionStatusClient {

    override suspend fun getStatus(request: TransactionStateRequest): Result<TransactionChages> {
        val tx = txStatusService.transaction(request.hash).getOrNull() ?: return Result.failure(Exception())
        val state = if (tx.successful == true) TransactionState.Confirmed else TransactionState.Failed
        return Result.success(
            TransactionChages(
                state = state,
                fee = try { tx.fee_charged.toBigInteger() } catch (_: Throwable) { null }
            )
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}