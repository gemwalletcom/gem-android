package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.stellar.services.StellarBroadcastService
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URLEncoder

class StellarBroadcastClient(
    private val chain: Chain,
    private val broadcastService: StellarBroadcastService
) : BroadcastClient {

    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): Result<String> {
        val sign = String(signedMessage)
        val encoded = "tx=${URLEncoder.encode(sign, "utf-8")}"
        val request = encoded.toRequestBody(Mime.Form.value)
        val resp = broadcastService.broadcast(request).getOrNull() ?: return Result.failure(Exception("Server return error"))
        return if (resp.hash.isNullOrEmpty()) {
            Result.failure(Exception(resp.title ?: "Broadcast transaction fail"))
        } else {
            Result.success(resp.hash)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}