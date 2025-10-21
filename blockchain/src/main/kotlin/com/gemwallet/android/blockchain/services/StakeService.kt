package com.gemwallet.android.blockchain.services

import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toChain
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import uniffi.gemstone.GemDelegationState
import uniffi.gemstone.GemGateway

class StakeService(
    private val gateway: GemGateway,
) {
    suspend fun getValidators(
        chain: Chain,
        apr: Double
    ): List<DelegationValidator> {
        return try {
            val result = gateway.getStakingValidators(
                chain = chain.string,
                apr,
            )
            result.mapNotNull { item ->
                DelegationValidator(
                    chain = item.chain.toChain() ?: return@mapNotNull null,
                    id = item.id,
                    name = item.name,
                    isActive = item.isActive,
                    commission = item.commission,
                    apr = item.apr,
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun getStakeDelegations(
        chain: Chain,
        address: String,
    ): List<DelegationBase> {
        return try {
            val result = gateway.getStakingDelegations(
                chain = chain.string,
                address,
            )
            result.mapNotNull { item ->
                DelegationBase(
                    assetId = item.assetId.toAssetId() ?: return@mapNotNull  null,
                    state = when (item.state) {
                        GemDelegationState.ACTIVE -> DelegationState.Active
                        GemDelegationState.PENDING -> DelegationState.Pending
                        GemDelegationState.INACTIVE -> DelegationState.Inactive
                        GemDelegationState.ACTIVATING -> DelegationState.Activating
                        GemDelegationState.DEACTIVATING -> DelegationState.Deactivating
                        GemDelegationState.AWAITING_WITHDRAWAL -> DelegationState.AwaitingWithdrawal
                    },
                    balance = item.balance,
                    rewards = item.rewards,
                    completionDate = item.completionDate?.toLong(),
                    delegationId = item.delegationId,
                    validatorId = item.validatorId,
                    shares = item.shares,
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }
}