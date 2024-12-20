package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.stellar.services.StellarBroadcastService
import com.google.gson.Gson
import com.wallet.core.blockchain.stellar.StellarTransactionBroadcastError
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class StellarBroadcastClient(
    private val chain: Chain,
    private val broadcastService: StellarBroadcastService
) : BroadcastClient {

    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): Result<String> {
        val resp = broadcastService.broadcast(data = String(signedMessage))
        return if (resp.isSuccessful) {
            Result.success(resp.body()?.hash ?: throw Exception("Server return error"))
        } else {
            val message = try {
                Gson().fromJson(resp.errorBody()?.string(), StellarTransactionBroadcastError::class.java)?.title
            } catch (_: Throwable) {
                "Broadcast transaction fail"
            }
            throw Exception(message)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}