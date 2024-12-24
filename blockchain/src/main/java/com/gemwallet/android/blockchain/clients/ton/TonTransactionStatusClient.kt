package com.gemwallet.android.blockchain.clients.ton

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
    override suspend fun getStatus(request: TransactionStateRequest): Result<TransactionChages> {
        val txHashData = Base64.decode(request.hash)
        return rpcClient.transaction(txHashData.toHexString(""))
            .mapCatching {
                val rawHash = it.firstOrNull()?.hash
                    ?: return@mapCatching TransactionChages(TransactionState.Pending)
                val newId = Base64.decode(rawHash).toHexString("")
                TransactionChages(TransactionState.Confirmed, hashChanges = HashChanges(old = request.hash, new = newId))
            }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}