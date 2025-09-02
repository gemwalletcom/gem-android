package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toChainType
import com.gemwallet.android.model.HashChanges
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import com.wallet.core.primitives.TransactionState
import okhttp3.internal.toLongOrDefault
import uniffi.gemstone.GemGateway
import uniffi.gemstone.GemTransactionChange
import uniffi.gemstone.GemTransactionStateRequest
import java.lang.IllegalArgumentException

class TransactionStatusService(
    private val gateway: GemGateway,
    private val stateClients: List<TransactionStatusClient>,
) {
    suspend fun getStatus(request: TransactionStateRequest): TransactionChanges? {
        return try {
            if (request.chain.toChainType() == ChainType.Ethereum) {
                stateClients.firstOrNull { it.supported(request.chain) }
                    ?.getStatus(request)
            } else {
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

                return TransactionChanges(
                    state = TransactionState.entries.firstOrNull { it.string == result.state } ?: throw ServiceUnavailable,
                    fee = fee,
                    hashChanges = hashChanges?.let { HashChanges(it.old, it.new) }
                )
            }
        } catch (_: Throwable) {
            throw ServiceUnavailable
        }
    }
}