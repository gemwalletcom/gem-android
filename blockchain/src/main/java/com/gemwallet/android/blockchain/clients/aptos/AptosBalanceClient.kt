package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain

class AptosBalanceClient(
    private val chain: Chain,
    private val rpcClient: AptosRpcClient,
) : BalanceClient {
    override suspend fun getNativeBalance(address: String): Balances? {
        return rpcClient.balance(address)
            .fold({ Balances.create(AssetId(chain), it.data.coin.value.toBigInteger()) }) { null }
    }

    override suspend fun getTokenBalances(address: String, tokens: List<AssetId>): List<Balances> = emptyList()

    override fun maintainChain(): Chain = chain
}