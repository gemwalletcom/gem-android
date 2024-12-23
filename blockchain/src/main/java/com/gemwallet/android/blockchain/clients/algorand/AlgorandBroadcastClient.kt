package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandBroadcastService
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import okhttp3.RequestBody.Companion.toRequestBody

class AlgorandBroadcastClient(
    private val chain: Chain,
    private val broadcastService: AlgorandBroadcastService,
) : BroadcastClient {

    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): Result<String> {
        val result =  broadcastService.broadcast(signedMessage.toRequestBody(Mime.Binary.value))
        val resp = result.getOrNull()
            ?: return Result.failure(Exception("Broadcast transaction fail"))
        return if (resp.txId.isNullOrEmpty()) {
            Result.failure(Exception(resp.message ?: "Broadcast transaction fail"))
        } else {
            Result.success(resp.txId)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}