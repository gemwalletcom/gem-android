package com.gemwallet.android.blockchain.clients.tron

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
    override suspend fun getStatus(request: TransactionStateRequest): Result<TransactionChages> {
        return rpcClient.transaction(TronRpcClient.TronValue(request.hash)).mapCatching {
            if (it.receipt != null && it.receipt?.result == "OUT_OF_ENERGY") {
                return@mapCatching TransactionChages(TransactionState.Reverted)
            }
            if (it.result == "FAILED") {
                return@mapCatching TransactionChages(TransactionState.Reverted)
            }
            if (it.blockNumber > 0) {
                val fee = it.fee ?: 0
                return@mapCatching TransactionChages(TransactionState.Confirmed, BigInteger.valueOf(fee.toLong()))
            }
            TransactionChages(TransactionState.Pending)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}