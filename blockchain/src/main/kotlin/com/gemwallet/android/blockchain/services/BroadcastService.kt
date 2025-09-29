package com.gemwallet.android.blockchain.services

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.TransactionType
import uniffi.gemstone.BroadcastOptions
import uniffi.gemstone.GemGateway

class BroadcastService(
    private val gateway: GemGateway,
) {

    suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): String {
        val hash = gateway.transactionBroadcast(
            chain = account.chain.string,
            data = String(signedMessage),
            options = BroadcastOptions(
                when (account.chain) {
                    Chain.Solana -> type == TransactionType.Swap
                    else -> false
                }
            )
        )
        return hash
    }
}