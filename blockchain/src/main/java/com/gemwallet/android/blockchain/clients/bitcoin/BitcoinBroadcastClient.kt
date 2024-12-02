package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.rpc.RpcError
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import okhttp3.RequestBody.Companion.toRequestBody

class BitcoinBroadcastClient(
    private val chain: Chain,
    private val rpcClient: BitcoinRpcClient,
) : BroadcastClient {

    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): Result<String> {
        val requestBody = signedMessage.toHexString("").toRequestBody(Mime.Plain.value)
        return rpcClient.broadcast(requestBody).mapCatching {
            it.result ?: throw RpcError.BroadcastFail(it.error?.message ?: "Unknown error")
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}