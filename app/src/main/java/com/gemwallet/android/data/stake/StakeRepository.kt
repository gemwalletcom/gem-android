package com.gemwallet.android.data.stake

import com.gemwallet.android.blockchain.clients.StakeClient
import com.gemwallet.android.services.GemApiStaticClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import uniffi.Gemstone.Config
import java.math.BigInteger

class StakeRepository(
    private val gemApiStaticClient: GemApiStaticClient,
    private val localSource: StakeLocalSource,
    private val stakeClients: List<StakeClient>,
) {

    private val recommendedValidators = Config().getValidators()

    suspend fun sync(chain: Chain, address: String, apr: Double) = withContext(Dispatchers.IO) {
        syncValidators(chain, apr)
        syncDelegations(chain, address, apr)

    }

    private suspend fun syncDelegations(chain: Chain, address: String, apr: Double) {
        val delegations = try {
            stakeClients
                .firstOrNull { it.isMaintain(chain) }
                ?.getStakeDelegations(address, apr) ?: return
        } catch (err: Throwable) {
            return
        }
        localSource.update(address, delegations)
    }

    suspend fun syncValidators(chain: Chain? = null, apr: Double) {
        val validatorsInfo = gemApiStaticClient.getValidators(chain?.string ?: return)
            .getOrNull()
            ?.groupBy { it.id }
            ?.mapValues { it.value.firstOrNull() }
            ?: emptyMap()
        val validators = stakeClients
            .filter { it.isMaintain(chain) }
            .asFlow()
            .mapNotNull {
                try {
                    it.getValidators(apr)
                } catch (err: Throwable) {
                    null
                }
            }
            .flowOn(Dispatchers.IO)
            .toList()
            .flatten()
            .map {
                if (it.name.isEmpty()) {
                    it.copy(name = validatorsInfo[it.id]?.name ?: "")
                } else {
                    it
                }
            }
        localSource.update(validators)
    }

    fun getRecommendValidators(chain: Chain): List<String> {
        return recommendedValidators[chain.string] ?: emptyList()
    }

    suspend fun getRecommended(chain: Chain): DelegationValidator? {
        val validators = getValidators(chain).first()
        val recommendedId = getRecommendValidators(chain)
        return validators.firstOrNull { it.name.isNotEmpty() && recommendedId.contains(it.id) }
            ?: validators.firstOrNull { it.name.isNotEmpty() }
    }

    suspend fun getValidators(chain: Chain): Flow<List<DelegationValidator>> {
        return localSource.getValidators(chain)
    }

    suspend fun getDelegations(assetId: AssetId, owner: String): Flow<List<Delegation>> {
        return localSource.getDelegations(assetId, owner)
    }

    suspend fun getDelegation(validatorId: String, delegationId: String = ""): Flow<Delegation?> {
        return localSource.getDelegation(validatorId = validatorId, delegationId = delegationId)
    }

    suspend fun getRewards(assetId: AssetId, owner: String): List<Delegation> {
        return getDelegations(assetId, owner).first()
            .filter { BigInteger(it.base.rewards) > BigInteger.ZERO }
    }

    suspend fun getStakeValidator(assetId: AssetId, validatorId: String): DelegationValidator? {
        return localSource.getStakeValidator(assetId, validatorId)
    }

    suspend fun getUnstakeValidator(assetId: AssetId, address: String): DelegationValidator? {
        return localSource.getUnstakeValidator(assetId, address)
    }
}