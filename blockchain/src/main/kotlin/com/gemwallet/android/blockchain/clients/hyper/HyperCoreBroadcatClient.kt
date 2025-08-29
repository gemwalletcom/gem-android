package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.BroadcastClientProxy
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType

class HyperCoreBroadcatClient(
    private val chain: Chain,
) : BroadcastClient {
    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): String {
        TODO("Not yet implemented")
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}