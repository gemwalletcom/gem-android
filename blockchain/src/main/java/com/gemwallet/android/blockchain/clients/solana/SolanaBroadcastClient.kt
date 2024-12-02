package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import java.nio.charset.StandardCharsets

class SolanaBroadcastClient(
    private val chain: Chain,
    private val rpcClient: SolanaRpcClient,
) : BroadcastClient {
    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): Result<String> {
        val encodedMessage = signedMessage.toString(StandardCharsets.UTF_8)
        val params = if (type == TransactionType.Swap) {
            listOf(
                encodedMessage,
                mapOf(
                    "encoding" to "base64",
                    "skipPreflight" to true,
                ),
            )
        } else {
            listOf(
                encodedMessage,
                mapOf("encoding" to "base64"),
            )
        }
        val request = JSONRpcRequest.create(SolanaMethod.SendTransaction, params)
        return rpcClient.broadcast(request).mapCatching {
            if (it.error == null) it.result else throw Exception(it.error.message)
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}