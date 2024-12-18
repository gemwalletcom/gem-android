package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.aptos.services.AptosBroadcastService
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import okhttp3.RequestBody.Companion.toRequestBody

class AptosBroadcastClient(
    private val chain: Chain,
    private val broadcastService: AptosBroadcastService,
) : BroadcastClient {
    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): Result<String> = try {
        val hash = broadcastService.broadcast(String(signedMessage).toRequestBody(Mime.Json.value)).getOrThrow().hash
        Result.success(hash)
    } catch (err: Throwable) {
        Result.failure(err)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}