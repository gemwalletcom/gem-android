package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.total
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import java.math.BigInteger

class SuiBalanceClient(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient,
) : BalanceClient {
    override fun supported(chain: Chain): Boolean = this.chain == chain

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? {
        val amount = rpcClient.balance(address)
            .mapCatching { it.result.totalBalance.toBigInteger() }
            .getOrNull() ?: return null
        return AssetBalance.create(chain.asset(), available = amount.toString())
    }

    override suspend fun getDelegationBalances(chain: Chain, address: String): AssetBalance? {
        val delegations = rpcClient.delegations(JSONRpcRequest.create(SuiMethod.Delegations, listOf(address)))
            .getOrNull()?.result ?: emptyList()

        val staked = delegations.map {
            it.stakes.map { stake -> stake.total() }.fold(BigInteger.ZERO) { acc, value -> acc + value}
        }.fold(BigInteger.ZERO) {acc, value -> acc + value}

        return AssetBalance.create(chain.asset(), staked = staked.toString())
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> {
        return rpcClient.balances(address).mapCatching { response ->
            val balances = response.result
            tokens.mapNotNull { token ->
                AssetBalance.create(
                    token,
                    balances.firstOrNull{ token.id.tokenId == it.coinType }?.totalBalance ?: return@mapNotNull null,
                )
            }
        }.getOrNull() ?: emptyList()
    }
}