package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.stellar.services.StellarBroadcastService
import com.gemwallet.android.blockchain.rpc.ServiceError
import com.gemwallet.android.blockchain.rpc.responseFold
import com.wallet.core.blockchain.stellar.StellarTransactionBroadcast
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
        val encoded = "tx=${URLEncoder.encode(String(signedMessage), "utf-8")}"
        val resp = broadcastService.broadcast(encoded.toRequestBody(Mime.Form.value))

        return resp.responseFold(
            onSuccess = {
                Result.success(it?.hash ?: throw ServiceError.EmptyHash)
            },
            onError = { err: StellarTransactionBroadcast ->
                Result.failure(Exception(err.title))
            },
            onFailure = { cause ->
                val error = cause.message?.let { ServiceError.BroadCastError(it) }
                    ?: ServiceError.ServerError(err = cause)
                Result.failure(error)
            }
        )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}

