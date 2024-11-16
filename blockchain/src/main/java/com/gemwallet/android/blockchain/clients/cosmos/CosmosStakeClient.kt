package com.gemwallet.android.blockchain.clients.cosmos

import android.annotation.SuppressLint
import com.gemwallet.android.blockchain.clients.StakeClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.CosmosDenom
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.text.SimpleDateFormat

class CosmosStakeClient(
    private val chain: Chain,
    private val rpcClient: CosmosRpcClient,
) : StakeClient {

    @SuppressLint("SimpleDateFormat")
    private val completionDateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

    override suspend fun getValidators(chain: Chain, apr: Double): List<DelegationValidator> {
        return rpcClient.validators().getOrNull()?.validators?.map {
            val commission = it.commission.commission_rates.rate.toDouble()
            val isActive = !it.jailed && it.status == "BOND_STATUS_BONDED"
            DelegationValidator(
                chain = chain,
                id = it.operator_address,
                name = it.description.moniker,
                isActive = isActive,
                commision = commission,
                apr = if (isActive) apr - (apr * commission) else 0.0,
            )
        } ?: emptyList()
    }

    override suspend fun getStakeDelegations(chain: Chain, address: String, apr: Double): List<DelegationBase> = withContext(Dispatchers.IO) {
        val getDelegations = async { rpcClient.delegations(address).getOrNull()?.delegation_responses }
        val getUnboundingDelegations = async { rpcClient.undelegations(address).getOrNull()?.unbonding_responses }
        val getRewards = async {
            rpcClient.rewards(address).getOrNull()?.rewards
                ?.associateBy { it.validator_address }
                ?.mapValues { entry ->
                    entry.value.reward
                        .filter { it.denom == CosmosDenom.from(chain) }
                        .mapNotNull {
                            try {
                                BigInteger(it.amount.split(".")[0])
                            } catch (_: Throwable) {
                                BigInteger.ZERO
                            }
                        }
                        .reduceOrNull { acc, value -> acc + value }
                        ?.toString()
                } ?: emptyMap()
        }
        val delegations = getDelegations.await()
        val undelegations = getUnboundingDelegations.await()
        val rewards = getRewards.await()

        val baseDelegations = delegations?.map {
            DelegationBase(
                assetId = AssetId(chain),
                state = DelegationState.Active,
                balance = it.balance.amount,
                completionDate = null,
                delegationId = "",
                validatorId = it.delegation.validator_address,
                rewards = rewards[it.delegation.validator_address] ?: "0",
                shares = "",
            )
        } ?: emptyList()

        val baseUndelegations: List<DelegationBase> = undelegations?.map { undelegation ->
            undelegation.entries.map { entry ->
                DelegationBase(
                    assetId = AssetId(chain),
                    state = DelegationState.Pending,
                    balance = entry.balance,
                    completionDate = completionDateFormatter.parse(entry.completion_time)?.time, //2024-02-28T08:12:56.376120563Z
                    delegationId = entry.creation_height,
                    validatorId = undelegation.validator_address,
                    rewards = rewards[undelegation.validator_address] ?: "0",
                    shares = "",
                )
            }
        }?.flatten() ?: emptyList()

        (baseDelegations + baseUndelegations)
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain
}