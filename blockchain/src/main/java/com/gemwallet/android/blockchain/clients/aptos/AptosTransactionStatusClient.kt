package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState

class AptosTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: AptosRpcClient,
) : TransactionStatusClient {
    override suspend fun getStatus(owner: String, txId: String): Result<TransactionChages> {
        val status = if (rpcClient.transactions(txId).getOrNull()?.success == true) {
            TransactionState.Confirmed
        } else {
            TransactionState.Pending
        }
        return Result.success(TransactionChages(status))
    }

    override fun maintainChain(): Chain = chain
}