package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.clients.BroadcastClient
import com.gemwallet.android.blockchain.clients.getClient
import com.gemwallet.android.ext.toChainType
import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import com.wallet.core.primitives.TransactionType
import uniffi.gemstone.GemBroadcastOptions
import uniffi.gemstone.GemGateway

class BroadcastClientProxy(
    private val gateway: GemGateway,
    private val clients: List<BroadcastClient>,
) : BroadcastClient {

    override suspend fun send(
        account: Account,
        signedMessage: ByteArray,
        type: TransactionType
    ): String {
        return if (account.chain.toChainType() == ChainType.Ethereum) {
            clients.getClient(account.chain)?.send(account, signedMessage, type)
                ?: throw Exception("Chain isn't support")
        } else {
            val hash = gateway.transactionBroadcast(
                chain = account.chain.string,
                data = String(signedMessage),
                options = GemBroadcastOptions(
                    when (account.chain) {
                        Chain.Solana -> type == TransactionType.Swap
                        else -> false
                    }
                )
            )
            hash
        }
    }

    override fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}