package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.Mime
import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.bitcoin.services.BitcoinBroadcastService
import com.gemwallet.android.blockchain.rpc.RpcError
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import okhttp3.RequestBody.Companion.toRequestBody

class BitcoinBroadcastClient(
    private val chain: Chain,
    private val broadcastService: BitcoinBroadcastService,
) : BroadcastClient {

    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): String {
        val requestBody = signedMessage.toHexString("").toRequestBody(Mime.Plain.value)
        return broadcastService.broadcast(requestBody).getOrThrow().result
            ?: throw RpcError.BroadcastFail("Unknown error")
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}