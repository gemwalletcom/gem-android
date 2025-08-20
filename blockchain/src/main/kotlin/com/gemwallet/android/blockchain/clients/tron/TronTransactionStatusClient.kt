package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import java.math.BigInteger

class TronTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: TronRpcClient
) : TransactionStatusClient {
    override suspend fun getStatus(request: TransactionStateRequest): TransactionChanges {
        val resp = rpcClient.transaction(TronRpcClient.TronValue(request.hash)).getOrNull() ?: throw ServiceUnavailable
        return when {
            resp.receipt != null && resp.receipt?.result == "OUT_OF_ENERGY" -> TransactionChanges(TransactionState.Reverted)
            resp.result == "FAILED" -> TransactionChanges(TransactionState.Reverted)
            resp.blockNumber > 0 -> {
                val fee = resp.fee ?: 0
                return TransactionChanges(TransactionState.Confirmed, BigInteger.valueOf(fee.toLong()))
            }
            else -> TransactionChanges(TransactionState.Pending)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}