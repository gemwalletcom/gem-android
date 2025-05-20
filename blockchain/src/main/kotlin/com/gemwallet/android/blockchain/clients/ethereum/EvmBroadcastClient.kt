package com.gemwallet.android.blockchain.clients.ethereum

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.ethereum.services.EvmBroadcastService
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class EvmBroadcastClient(
    private val chain: Chain,
    private val broadcastService: EvmBroadcastService,
) : BroadcastClient {
    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): String {
        val request = JSONRpcRequest.create(EvmMethod.Broadcast, listOf(signedMessage.toHexString()))
        val result = broadcastService.broadcast(request)
        val data = result.getOrNull() ?: throw Exception(result.exceptionOrNull()?.message ?: "Network error")
        if (data.error != null) {
            throw Exception(data.error.message)
        }
        return data.result
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}