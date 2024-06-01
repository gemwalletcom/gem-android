package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.total
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

class SuiBalanceClient(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient,
) : BalanceClient {
    override fun maintainChain(): Chain = chain

    override suspend fun getNativeBalance(address: String): Balances = withContext(Dispatchers.IO) {
        val amountJob = async {
            rpcClient.balance(address).mapCatching {
                it.result.totalBalance.toBigInteger()
            }.getOrNull() ?: BigInteger.ZERO
        }
        val delegationsJob = async {
            rpcClient.delegations(JSONRpcRequest.create(SuiMethod.Delegations, listOf(address)))
                .getOrNull()?.result ?: emptyList()
        }
        val amount = amountJob.await()
        val delegations = delegationsJob.await()
        val staked = delegations.map {
            it.stakes.map { stake -> stake.total() }.fold(BigInteger.ZERO) { acc, value -> acc + value}
        }.fold(BigInteger.ZERO) {acc, value -> acc + value}
        Balances.create(AssetId(chain), amount, staked = staked)
    }

    override suspend fun getTokenBalances(address: String, tokens: List<AssetId>): List<Balances> {
        return rpcClient.balances(address).mapCatching { response ->
            val balances = response.result
            tokens.mapNotNull { assetId ->
                Balances.create(
                    assetId,
                    balances.firstOrNull{ assetId.tokenId == it.coinType }?.totalBalance?.toBigInteger() ?: return@mapNotNull null,
                )
            }
        }.getOrNull() ?: emptyList()
    }
}