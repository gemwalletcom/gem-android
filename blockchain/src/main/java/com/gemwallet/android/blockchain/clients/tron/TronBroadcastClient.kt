package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.rpc.RpcError
import com.gemwallet.android.blockchain.rpc.ServiceError
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import okhttp3.RequestBody.Companion.toRequestBody

class TronBroadcastClient(
    private val chain: Chain,
    private val rpcClient: TronRpcClient,
) : BroadcastClient {

    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): String {
        val requestData = signedMessage.toRequestBody(Mime.Json.value)
        val data = rpcClient.broadcast(requestData).getOrNull() ?: throw ServiceError.NetworkError
        if (data.result == true) {
            return data.txid
        }
        throw RpcError.TransactionSendError
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}