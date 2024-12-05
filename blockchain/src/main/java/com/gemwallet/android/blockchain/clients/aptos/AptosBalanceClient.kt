package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.aptos.rpc.AptosRpcClient
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain

class AptosBalanceClient(
    private val chain: Chain,
    private val rpcClient: AptosRpcClient,
) : BalanceClient {
    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        return rpcClient.balance(address)
            .fold({
                AssetBalance.create(chain.asset(), available = it.data.coin.value)
            }) { null }
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> = emptyList()

    override fun supported(chain: Chain): Boolean = this.chain == chain
}