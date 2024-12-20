package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandBroadcastService
import com.google.gson.Gson
import com.wallet.core.blockchain.algorand.AlgorandTransactionBroadcastError
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class AlgorandBroadcastClient(
    private val chain: Chain,
    private val broadcastService: AlgorandBroadcastService,
) : BroadcastClient {

    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): Result<String> {
        val resp =  broadcastService.broadcast(signedMessage)
        return if (resp.isSuccessful) {
            Result.success(resp.body()?.txId ?: throw Exception("Broadcast transaction fail"))
        } else {
            val message = try {
                Gson().fromJson(resp.errorBody()?.string(), AlgorandTransactionBroadcastError::class.java).message
            } catch (_: Throwable) {
                "Broadcast transaction fail"
            }
            throw Exception(message)
        }
    }

    override fun supported(chain: Chain): Boolean {
        TODO("Not yet implemented")
    }
}