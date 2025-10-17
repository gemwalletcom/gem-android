package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.model.ServiceUnavailable
import com.gemwallet.android.blockchain.model.TransactionStateRequest
import com.gemwallet.android.model.HashChanges
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.TransactionState
import okhttp3.internal.toLongOrDefault
import uniffi.gemstone.GemGateway
import uniffi.gemstone.GemTransactionStateRequest
import uniffi.gemstone.TransactionChange

class TransactionStatusService(
    private val gateway: GemGateway,
) {
    suspend fun getStatus(request: TransactionStateRequest): TransactionChanges? {
        return try {
            val result = gateway.getTransactionStatus(
                chain = request.chain.string,
                GemTransactionStateRequest(
                    id = request.hash,
                    senderAddress = request.sender,
                    createdAt = 0L, /// TODO: Add created at for HyperCore,
                    blockNumber = request.block.toLongOrDefault(0L) ,
                )
            )
            val fee = result.changes.firstNotNullOfOrNull { it as? TransactionChange.NetworkFee }
                ?.v1?.toBigIntegerOrNull()
            val hashChanges = result.changes.firstNotNullOfOrNull { it as? TransactionChange.HashChange }

            TransactionChanges(
                state = when (result.state) {
                    uniffi.gemstone.TransactionState.PENDING -> TransactionState.Pending
                    uniffi.gemstone.TransactionState.CONFIRMED -> TransactionState.Confirmed
                    uniffi.gemstone.TransactionState.FAILED -> TransactionState.Failed
                    uniffi.gemstone.TransactionState.REVERTED -> TransactionState.Reverted
                },
                fee = fee,
                hashChanges = hashChanges?.let { HashChanges(it.old, it.new) }
            )
        } catch (_: Throwable) {
            throw ServiceUnavailable
        }
    }
}