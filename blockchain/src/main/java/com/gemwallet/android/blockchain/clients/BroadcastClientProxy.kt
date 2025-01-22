package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class BroadcastClientProxy(
    private val clients: List<BroadcastClient>,
) : BroadcastClient {

    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): String {
        return clients.getClient(account.chain)?.send(account, signedMessage, type)
            ?: throw Exception("Chain isn't support")
    }

    override fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}