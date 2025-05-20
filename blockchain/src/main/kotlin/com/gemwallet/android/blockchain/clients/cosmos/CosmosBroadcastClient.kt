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

    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): String {
        val requestData = signedMessage.toString(Charsets.UTF_8)
        val requestBody = requestData.toRequestBody(Mime.Json.value)
        val result = broadcastService.broadcast(requestBody)
        val data = result.getOrNull() ?: throw Exception(result.exceptionOrNull()?.message ?: "Broadcast error")
        if (data.tx_response.code != 0) {
            throw Exception(data.tx_response.raw_log)
        }
        return data.tx_response.txhash
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}