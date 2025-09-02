package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.clients.StakeClient
import com.gemwallet.android.blockchain.clients.getClient
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toChain
import com.gemwallet.android.ext.toChainType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import uniffi.gemstone.GemGateway

class StakeClientProxy(
    private val gateway: GemGateway,
    private val clients: List<StakeClient>
) : StakeClient {
    override suspend fun getValidators(
        chain: Chain,
        apr: Double
    ): List<DelegationValidator> {
        return try {
            if (chain.toChainType() == ChainType.Ethereum) {
                clients.getClient(chain)?.getValidators(chain, apr) ?: emptyList()
            } else {
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
                        commision = item.commission,
                        apr = item.apr,
                    )
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    override suspend fun getStakeDelegations(
        chain: Chain,
        address: String,
        apr: Double
    ): List<DelegationBase> {
        return try {
            if (chain.toChainType() == ChainType.Ethereum) {
                clients.getClient(chain)?.getStakeDelegations(chain, address, apr) ?: emptyList()
            } else {
                val result = gateway.getStakingDelegations(
                    chain = chain.string,
                    address,
                )
                result.mapNotNull { item ->
                    DelegationBase(
                        assetId = item.assetId.toAssetId() ?: return@mapNotNull  null,
                        state = DelegationState.entries.firstOrNull { it.string == item.delegationState } ?: return@mapNotNull null,
                        balance = item.balance,
                        rewards = item.rewards,
                        completionDate = item.completionDate?.toLong(),
                        delegationId = item.delegationId,
                        validatorId = item.validatorId,
                        shares = item.shares,
                    )
                }
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    override fun supported(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}