package com.gemwallet.android.blockchain.clients.cardano

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.cardano.services.CardanoBroadcastService
import com.gemwallet.android.blockchain.clients.cardano.services.broadcast
import com.gemwallet.android.blockchain.rpc.ServiceError
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class CardanoBroadcastClient(
    private val chain: Chain,
    private val broadcastService: CardanoBroadcastService
) : BroadcastClient {
    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): Result<String> {
        val hash = broadcastService.broadcast(signedMessage.toHexString(prefix = ""))?.submitTransaction?.hash
            ?: return Result.failure(ServiceError.EmptyHash)
        return Result.success(hash)
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

}