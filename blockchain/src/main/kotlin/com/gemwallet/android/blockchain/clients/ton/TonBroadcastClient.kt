package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.rpc.ServiceError
import com.gemwallet.android.blockchain.rpc.ServiceError.EmptyHash
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import wallet.core.jni.Base64
import java.nio.charset.StandardCharsets

class TonBroadcastClient(
    private val chain: Chain,
    private val rpcClient: TonRpcClient,
) : BroadcastClient {
    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): String {
        val encodedMessage = signedMessage.toString(StandardCharsets.UTF_8)
        val response = rpcClient.broadcast(TonRpcClient.Boc(encodedMessage)).getOrNull()
            ?: throw ServiceError.NetworkError
        val hash = try {
            Base64.decode(response.result.hash).toHexString("")
        } catch (_: Throwable) {
            throw EmptyHash
        }
        return hash
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}