package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.algorand.services.AlgorandBroadcastService
import com.gemwallet.android.blockchain.rpc.ServiceError
import com.gemwallet.android.blockchain.rpc.handleError
import com.wallet.core.blockchain.algorand.AlgorandTransactionBroadcast
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
        return result.getOrNull()?.txId?.let { Result.success(it) }
            ?: Result.failure(
                result.handleError<AlgorandTransactionBroadcast>()?.message?.let { Exception(it) }
                    ?: ServiceError.BroadCastError()
            )
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}