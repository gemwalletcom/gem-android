package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.rpc.ServiceError
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class SuiBroadcastClient(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient,
) : BroadcastClient {

    override suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): String {
        val parts = String(signedMessage).split("_")
        val data = parts.first()
        val sign = parts[1]
        return rpcClient.broadcast(data, sign).getOrNull()?.result?.digest ?: throw ServiceError.NetworkError
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain

}