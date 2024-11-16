package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.StakeClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.gemwallet.android.ext.asset
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class SuiStakeClient(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient
): StakeClient {
    override suspend fun getValidators(chain: Chain, apr: Double): List<DelegationValidator> {
        val validators = rpcClient.validators(JSONRpcRequest.create(SuiMethod.Validators, listOf()))
            .getOrNull()?.result?.apys ?: return emptyList()
        return validators.map {
            DelegationValidator(
                chain = chain,
                id = it.address,
                name = "",
                isActive = true,
                commision = 0.0,
                apr = apr,
            )

        }
    }

    override suspend fun getStakeDelegations(chain: Chain, address: String, apr: Double): List<DelegationBase> = withContext(Dispatchers.IO) {
        val getDelegations = async {
            rpcClient.delegations(JSONRpcRequest.create(SuiMethod.Delegations, listOf(address)))
                .getOrNull()?.result
        }
        val getSystemState = async { rpcClient.systemState().getOrNull() }

        val delegations = getDelegations.await() ?: return@withContext emptyList()
        val systemState = getSystemState.await() ?: return@withContext emptyList()
        val nextEpoch = (systemState.result.epochStartTimestampMs.toLongOrNull() ?: 0L) +
                (systemState.result.epochDurationMs.toLongOrNull() ?: 0L)

        delegations.map { delegation ->
            delegation.stakes.map { stake ->
                val state = when (stake.status) {
                    "Active" -> DelegationState.Active
                    "Pending" -> DelegationState.Activating
                    else -> DelegationState.Pending
                }

                DelegationBase(
                    assetId = chain.asset().id,
                    state = state,
                    rewards = stake.estimatedReward ?: "0",
                    delegationId = stake.stakedSuiId,
                    validatorId = delegation.validatorAddress,
                    balance = stake.principal,
                    completionDate = nextEpoch,
                    shares = "",
                )
            }
        }.flatten()
    }

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain
}