package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.model.Balances
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.CosmosDenom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

class CosmosBalanceClient(
    private val chain: Chain,
    private val rpcClient: CosmosRpcClient,
) : BalanceClient {

    override suspend fun getNativeBalance(address: String): Balances? = withContext(Dispatchers.IO) {
        val assetId = AssetId(chain)
        val denom = CosmosDenom.from(chain)

        val getBalances = async { rpcClient.getBalance(address).getOrNull()?.balances }
        val balance = getBalances.await()
            ?.filter { it.denom == denom }
            ?.map { it.amount.toBigDecimal().toBigInteger() }
            ?.reduceOrNull { acc, value -> acc + value} ?: BigInteger.ZERO

        when (chain) {
            Chain.Thorchain -> {
                Balances.create(AssetId(chain), balance)
            }
            else -> {
                val getDelegations = async { rpcClient.delegations(address).getOrNull()?.delegation_responses }
                val getUnboundingDelegations = async { rpcClient.undelegations(address).getOrNull()?.unbonding_responses }
                val getRewards = async { rpcClient.rewards(address).getOrNull()?.rewards }

                val delegations = getDelegations.await()
                    ?.filter { it.balance.denom == denom }
                    ?.map { it.balance.amount.toBigDecimal().toBigInteger() }
                    ?.reduceOrNull { acc, value -> acc + value} ?: BigInteger.ZERO
                val undelegations = getUnboundingDelegations.await()
                    ?.mapNotNull { entry -> entry.entries.map { it.balance.toBigDecimal().toBigInteger() }.reduceOrNull { acc, value -> acc + value } }
                    ?.reduceOrNull { acc, value -> acc + value } ?: BigInteger.ZERO
                val rewards = getRewards.await()
                    ?.mapNotNull { reward ->
                        reward.reward
                            .filter { it.denom == denom }
                            .map { it.amount.toBigDecimal().toBigInteger() }
                            .reduceOrNull { acc, value -> acc + value}
                    }?.reduceOrNull { acc, value -> acc + value } ?: BigInteger.ZERO
                Balances.create(
                    assetId = assetId,
                    available = balance,
                    staked = delegations,
                    pending = undelegations,
                    rewards = rewards,
                    locked = BigInteger.ZERO,
                    frozen = BigInteger.ZERO,
                )
            }
        }
    }

    override suspend fun getTokenBalances(address: String, tokens: List<AssetId>): List<Balances> {
        val balances = try {
            rpcClient.getBalance(address).getOrNull() ?: return emptyList()
        } catch (err: Throwable) {
            return emptyList()
        }
        return tokens.map {  assetId ->
            val amount = balances.balances.firstOrNull { it.denom == assetId.tokenId }?.amount ?: "0"
            Balances.create(
                assetId,
                available = try {
                    BigInteger(amount)
                } catch (err: Throwable) {
                    return@map null
                }
            )
        }.mapNotNull { it }
    }

    override fun maintainChain(): Chain = chain
}

fun CosmosDenom.Companion.from(chain: Chain): String = when (chain) {
    Chain.Cosmos -> CosmosDenom.Uatom.string
    Chain.Osmosis -> CosmosDenom.Uosmo.string
    Chain.Thorchain -> CosmosDenom.Rune.string
    Chain.Celestia -> CosmosDenom.Utia.string
    Chain.Injective -> CosmosDenom.Inj.string
    Chain.Sei -> CosmosDenom.Usei.string
    Chain.Noble -> CosmosDenom.Uusdc.string
    else -> throw IllegalArgumentException("Coin is not supported")
}