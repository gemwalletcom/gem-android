package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.ServiceUnavailable
import com.gemwallet.android.blockchain.clients.TransactionNotFound
import com.gemwallet.android.blockchain.clients.TransactionStateRequest
import com.gemwallet.android.blockchain.clients.TransactionStatusClient
import com.gemwallet.android.math.toHexString
import com.gemwallet.android.model.HashChanges
import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionState
import wallet.core.jni.Base64

class TonTransactionStatusClient(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) : TransactionStatusClient {

    override suspend fun getStatus(request: TransactionStateRequest): TransactionChages {
        val txHashData = Base64.decode(request.hash)
        val resp = rpcClient.transaction(txHashData.toHexString("")).getOrNull() ?: throw ServiceUnavailable

        val transaction = resp.transactions.firstOrNull() ?: throw TransactionNotFound()
        val newId = Base64.decode(transaction.hash).toHexString("")
        val transactionState = when {
            transaction.out_msgs.firstOrNull()?.let { it.bounce && it.bounced } != false -> TransactionState.Failed
            else -> TransactionState.Confirmed
        }
        return TransactionChages(
            state = transactionState,
            hashChanges = HashChanges(old = request.hash, new = newId)
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}