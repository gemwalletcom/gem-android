package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionError
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class NearTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: NearRpcClient,
) : TransactionStatusClient {
    override suspend fun getStatus(request: TransactionStateRequest): TransactionChanges {
        val resp =  rpcClient.transaction(
            JSONRpcRequest(
                method = NearMethod.Transaction.value,
                params = mapOf(
                    "tx_hash" to request.hash,
                    "sender_account_id" to request.sender,
                    "wait_until" to  "EXECUTED",
                ),
            )
        ).getOrNull() ?: throw ServiceUnavailable
        if (resp.error != null) {
            throw TransactionError(resp.error.message)
        }
        return when (resp.result.final_execution_status) {
            "FINAL" -> TransactionChanges(state = TransactionState.Confirmed)
            else -> TransactionChanges(state = TransactionState.Pending)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}