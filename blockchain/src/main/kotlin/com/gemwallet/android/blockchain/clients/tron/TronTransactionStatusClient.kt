package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import java.math.BigInteger

class TronTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: TronRpcClient
) : TransactionStatusClient {
    override suspend fun getStatus(request: TransactionStateRequest): TransactionChages {
        val resp = rpcClient.transaction(TronRpcClient.TronValue(request.hash)).getOrNull() ?: throw ServiceUnavailable
        return when {
            resp.receipt != null && resp.receipt?.result == "OUT_OF_ENERGY" -> TransactionChages(TransactionState.Reverted)
            resp.result == "FAILED" -> TransactionChages(TransactionState.Reverted)
            resp.blockNumber > 0 -> {
                val fee = resp.fee ?: 0
                return TransactionChages(TransactionState.Confirmed, BigInteger.valueOf(fee.toLong()))
            }
            else -> TransactionChages(TransactionState.Pending)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}