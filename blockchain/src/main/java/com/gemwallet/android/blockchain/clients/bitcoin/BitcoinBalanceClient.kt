package com.gemwallet.android.blockchain.clients.bitcoin

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain

class BitcoinBalanceClient(
    private val chain: Chain,
    private val rpcClient: BitcoinRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        return rpcClient.getBalance(address)
            .fold(
                {
                    if (it.balance != null) {
                        AssetBalance.create(chain.asset(), available = it.balance)
                    } else {
                        null
                    }
                }
            ) { null }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}