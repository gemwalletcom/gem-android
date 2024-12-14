package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class SuiBroadcastClient(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient,
) : BroadcastClient {

    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): Result<String> {
        val parts = String(signedMessage).split("_")
        val data = parts.first()
        val sign = parts[1]
        return rpcClient.broadcast(data, sign).mapCatching {
            it.result.digest
        }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

}