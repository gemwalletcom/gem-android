package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import okhttp3.RequestBody.Companion.toRequestBody

class CosmosBroadcastClient(
    private val chain: Chain,
    private val client: CosmosRpcClient,
) : BroadcastClient {

    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): Result<String> {
        val requestData = signedMessage.toString(Charsets.UTF_8)
        val requestBody = requestData.toRequestBody(Mime.Json.value)
        return client.broadcast(requestBody).mapCatching {
            if (it.tx_response.code != 0) {
                throw IllegalStateException(it.tx_response.raw_log)
            }
            it.tx_response.txhash
        }
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain
}