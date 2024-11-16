package com.gemwallet.android.data.repositoreis.stake

import com.gemwallet.android.blockchain.clients.StakeClient
import com.gemwallet.android.data.service.store.database.StakeDao
import com.gemwallet.android.data.service.store.database.mappers.DelegationBaseMapper
import com.gemwallet.android.data.service.store.database.mappers.DelegationValidatorMapper
import com.gemwallet.android.data.services.gemapi.GemApiStaticClient
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config
import java.math.BigInteger

class StakeRepository(
    private val gemApiStaticClient: GemApiStaticClient,
    private val stakeClients: List<StakeClient>,
    private val stakeDao: StakeDao,
) {

    private val validatorMapper = DelegationValidatorMapper()
    private val delegationMapper = DelegationBaseMapper()
    private val recommendedValidators = Config().getValidators()

    suspend fun sync(chain: Chain, address: String, apr: Double) = withContext(Dispatchers.IO) {
        syncValidators(chain, apr)
        syncDelegations(chain, address, apr)

    }

    private suspend fun syncDelegations(chain: Chain, address: String, apr: Double) = withContext(Dispatchers.IO) {
        val delegations = try {
            stakeClients.firstOrNull { it.isMaintain(chain) }?.getStakeDelegations(chain, address, apr) ?: return@withContext
        } catch (_: Throwable) {
            return@withContext
        }
        update(address, delegations)
    }

    suspend fun syncValidators(chain: Chain? = null, apr: Double) = withContext(Dispatchers.IO) {
        val validatorsInfo = gemApiStaticClient.getValidators(chain?.string ?: return@withContext)
            .getOrNull()
            ?.groupBy { it.id }
            ?.mapValues { it.value.firstOrNull() }
            ?: emptyMap()
        val validators = stakeClients
            .filter { it.isMaintain(chain) }
            .asFlow()
            .mapNotNull {
                try {
                    it.getValidators(chain, apr)
                } catch (_: Throwable) {
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
        update(validators)
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

    fun getValidators(chain: Chain): Flow<List<DelegationValidator>> {
        return stakeDao.getValidators(chain)
            .map { items ->
                items.map(validatorMapper::asDomain).filter { it.isActive }.sortedByDescending { it.apr }
            }
    }

    fun getDelegations(assetId: AssetId, owner: String): Flow<List<Delegation>> {
        return stakeDao.getDelegations(assetId.toIdentifier(), owner)
            .map { items -> items.mapNotNull { it.toModel() } }
    }

    fun getDelegation(validatorId: String, delegationId: String = ""): Flow<Delegation?> {
        return stakeDao.getDelegation(validatorId = validatorId, delegationId = delegationId)
            .map { it?.toModel() }
    }

    suspend fun getRewards(assetId: AssetId, owner: String): List<Delegation> {
        return getDelegations(assetId, owner).first()
            .filter { BigInteger(it.base.rewards) > BigInteger.ZERO }
    }

    suspend fun getStakeValidator(assetId: AssetId, validatorId: String): DelegationValidator? = withContext(Dispatchers.IO) {
        stakeDao.getStakeValidator(assetId.chain, validatorId)?.let { validatorMapper.asDomain(it) }
    }

    suspend fun getUnstakeValidator(assetId: AssetId, address: String): DelegationValidator? = withContext(Dispatchers.IO) {
        getDelegations(assetId, address).toList().firstOrNull()?.firstOrNull()?.validator
    }

    private suspend fun update(address: String, delegations: List<DelegationBase>) {
        if (delegations.isNotEmpty()) {
            val baseDelegations = delegations.map { delegationMapper.asEntity(it, { address }) }
            stakeDao.update(baseDelegations)
        } else {
            stakeDao.deleteBaseDelegation(address)
        }
    }

    suspend fun update(validators: List<DelegationValidator>) {
        stakeDao.updateValidators(validators.map(validatorMapper::asEntity))
    }
}