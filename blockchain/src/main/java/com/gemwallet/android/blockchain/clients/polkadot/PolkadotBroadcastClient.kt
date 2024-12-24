package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.polkadot.services.PolkadotBroadcastService
import com.gemwallet.android.math.toHexString
import com.wallet.core.blockchain.polkadot.PolkadotTransactionPayload
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class PolkadotBroadcastClient(
    private val chain: Chain,
    private val broadcastService: PolkadotBroadcastService,
) : BroadcastClient {
    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): Result<String> {
        val resp = broadcastService.broadcast(PolkadotTransactionPayload(signedMessage.toHexString())).getOrNull()
            ?: return Result.failure(Exception("Polkadot service unavailable"))
        return if (resp.hash.isNullOrEmpty()) {
            Result.failure(Exception(resp.cause ?: resp.error ?: "Broadcast error"))
        } else {
            Result.success(resp.hash)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}