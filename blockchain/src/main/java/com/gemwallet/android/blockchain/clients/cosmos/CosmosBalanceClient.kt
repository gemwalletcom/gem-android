package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosBalancesService
import com.gemwallet.android.blockchain.clients.cosmos.services.CosmosStakeService
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.CosmosDenom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger

class CosmosBalanceClient(
    private val chain: Chain,
    private val balancesService: CosmosBalancesService,
    private val stakeService: CosmosStakeService,
) : BalanceClient {

    override suspend fun getNativeBalance(chain: Chain, address: String): AssetBalance? = withContext(Dispatchers.IO) {
        val denom = CosmosDenom.from(chain)

        val getBalances = async { balancesService.getBalance(address).getOrNull()?.balances }
        val balance = getBalances.await()
            ?.filter { it.denom == denom }
            ?.map { it.amount.toBigDecimal().toBigInteger() }
            ?.reduceOrNull { acc, value -> acc + value} ?: BigInteger.ZERO

        when (chain) {
            Chain.Thorchain -> {
                AssetBalance.create(chain.asset(), available =  balance.toString())
            }
            else -> {
                val getDelegations = async { stakeService.delegations(address).getOrNull()?.delegation_responses }
                val getUnboundingDelegations = async { stakeService.undelegations(address).getOrNull()?.unbonding_responses }
                val getRewards = async { stakeService.rewards(address).getOrNull()?.rewards }

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
                AssetBalance.create(
                    asset = chain.asset(),
                    available = balance.toString(),
                    staked = delegations.toString(),
                    pending = undelegations.toString(),
                    rewards = rewards.toString(),
                )
            }
        }
    }

    override suspend fun getTokenBalances(chain: Chain, address: String, tokens: List<Asset>): List<AssetBalance> {
        val balances = try {
            balancesService.getBalance(address).getOrNull() ?: return emptyList()
        } catch (_: Throwable) {
            return emptyList()
        }
        return tokens.map {  asset ->
            val amount = balances.balances.firstOrNull { it.denom == asset.id.tokenId }?.amount ?: "0"
            AssetBalance.create(
                asset,
                available = try {
                    amount
                } catch (_: Throwable) {
                    return@map null
                }
            )
        }.mapNotNull { it }
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
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