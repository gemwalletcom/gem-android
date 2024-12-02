package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class NearBroadcastClient(
    private val chain: Chain,
    private val rpcClient: NearRpcClient,
) : BroadcastClient {
    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): Result<String> {
        return rpcClient.broadcast(
            JSONRpcRequest(
                NearMethod.Broadcast.value,
                params = mapOf(
                    "signed_tx_base64" to String(signedMessage)
                )
            )
        ).mapCatching {
            it.result.transaction.hash
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}