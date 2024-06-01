package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain

class BitcoinBalanceClient(
    private val chain: Chain,
    private val rpcClient: BitcoinRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(address: String): Balances? {
        return rpcClient.getBalance(address)
            .fold(
                {
                    if (it.balance != null) {
                        Balances.create(AssetId(chain), it.balance.toBigInteger())
                    } else {
                        null
                    }
                }
            ) { null }
    }

    override suspend fun getTokenBalances(address: String, tokens: List<AssetId>): List<Balances> = emptyList()

    override fun maintainChain(): Chain = chain
}