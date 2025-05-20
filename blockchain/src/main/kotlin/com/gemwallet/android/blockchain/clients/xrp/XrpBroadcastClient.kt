package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.rpc.ServiceError
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class XrpBroadcastClient(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) : BroadcastClient {
    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): String {
        val response = rpcClient.broadcast(signedMessage.toHexString("")).getOrNull()
            ?: throw ServiceError.NetworkError

        if (response.result.accepted != true && !response.result.engine_result_message.isNullOrEmpty()) {
            throw Exception(response.result.engine_result_message)
        }
        if (response.result.tx_json?.hash.isNullOrEmpty()) {
            throw Exception("Unable to get hash")
        }
        return response.result.tx_json?.hash!!
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

}