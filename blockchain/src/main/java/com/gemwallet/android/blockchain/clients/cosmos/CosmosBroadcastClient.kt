package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosBroadcastService
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import okhttp3.RequestBody.Companion.toRequestBody

class CosmosBroadcastClient(
    private val chain: Chain,
    private val broadcastService: CosmosBroadcastService,
) : BroadcastClient {

    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): Result<String> {
        val requestData = signedMessage.toString(Charsets.UTF_8)
        val requestBody = requestData.toRequestBody(Mime.Json.value)
        return broadcastService.broadcast(requestBody).mapCatching {
            if (it.tx_response.code != 0) {
                throw IllegalStateException(it.tx_response.raw_log)
            }
            it.tx_response.txhash
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}