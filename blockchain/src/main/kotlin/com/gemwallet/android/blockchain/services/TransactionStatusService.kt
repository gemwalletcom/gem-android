package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.model.ServiceUnavailable
import com.gemwallet.android.blockchain.model.TransactionStateRequest
import com.gemwallet.android.model.HashChanges
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.TransactionState
import okhttp3.internal.toLongOrDefault
import uniffi.gemstone.GemGateway
import uniffi.gemstone.GemTransactionChange
import uniffi.gemstone.GemTransactionStateRequest

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
            val fee = result.changes.firstNotNullOfOrNull { it as? GemTransactionChange.NetworkFee }
                ?.v1?.toBigIntegerOrNull()
            val hashChanges = result.changes.firstNotNullOfOrNull { it as? GemTransactionChange.HashChange }

            TransactionChanges(
                state = TransactionState.entries.firstOrNull { it.string == result.state } ?: throw ServiceUnavailable,
                fee = fee,
                hashChanges = hashChanges?.let { HashChanges(it.old, it.new) }
            )
        } catch (_: Throwable) {
            throw ServiceUnavailable
        }
    }
}