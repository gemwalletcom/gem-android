package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import java.nio.charset.StandardCharsets

class TonBroadcastClient(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) : BroadcastClient {
    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): Result<String> {
        val encodedMessage = signedMessage.toString(StandardCharsets.UTF_8)
        return rpcClient.broadcast(TonRpcClient.Boc(encodedMessage)).mapCatching { it.result.hash }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}