package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.math.toHexString
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class XrpBroadcastClient(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) : BroadcastClient {
    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): Result<String> {
        return rpcClient.broadcast(signedMessage.toHexString("")).mapCatching {
            if (!it.result.accepted && !it.result.engine_result_message.isNullOrEmpty()) {
                throw Exception(it.result.engine_result_message)
            }
            if (it.result.tx_json?.hash.isNullOrEmpty()) {
                throw Exception("Unable to get hash")
            }
            it.result.tx_json?.hash!!
        }
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain

}