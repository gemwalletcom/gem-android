package com.gemwallet.android.data.repositoreis.stake

import com.gemwallet.android.blockchain.services.StakeService
import com.gemwallet.android.data.service.store.database.StakeDao
import com.gemwallet.android.data.service.store.database.entities.toModel
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.GemApiStaticClient
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config
import java.math.BigInteger

class StakeRepository(
    private val gemApiStaticClient: GemApiStaticClient,
    private val gemApiClient: GemApiClient,
    private val stakeService: StakeService,
    private val stakeDao: StakeDao,
) {
    private val recommendedValidators = Config().getValidators()


    suspend fun sync(chain: Chain, address: String) = withContext(Dispatchers.IO) {
        val apr = try {
            gemApiClient.getAsset(chain.string, Currency.USD.string).properties.stakingApr ?: return@withContext
        } catch (_: Throwable) {
            return@withContext
        }
        syncValidators(chain, apr)
        syncDelegations(chain, address)
    }

    private suspend fun syncDelegations(chain: Chain, address: String) = withContext(Dispatchers.IO) {
        val delegations = try {
            stakeService.getStakeDelegations(chain, address)
        } catch (_: Throwable) {
            return@withContext
        }
        update(address, delegations)
    }

    suspend fun syncValidators(chain: Chain? = null, apr: Double) = withContext(Dispatchers.IO) {
        chain?.string ?: return@withContext
        val validatorsInfo = try {
            gemApiStaticClient.getValidators(chain.string)
                .groupBy { it.id }
                .mapValues { it.value.firstOrNull() }
        } catch (_: Throwable) {
            emptyMap()
        }
        val validators = stakeService.getValidators(chain, apr)
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

    fun getRecommended(chain: Chain): Flow<DelegationValidator?> {
        val validators = getValidators(chain)
        val recommendedId = getRecommendValidators(chain)
        return validators.map { items ->
            items.firstOrNull { it.name.isNotEmpty() && recommendedId.contains(it.id) } ?: items.firstOrNull { it.name.isNotEmpty() }
        }
    }

    fun getValidators(chain: Chain): Flow<List<DelegationValidator>> {
        return stakeDao.getValidators(chain)
            .map { items -> items.toModel().filter { it.isActive }.sortedByDescending { it.apr } }
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
        stakeDao.getStakeValidator(assetId.chain, validatorId)?.toModel()
    }

//    suspend fun getUnstakeValidator(assetId: AssetId, address: String): DelegationValidator? = withContext(Dispatchers.IO) {
//        getDelegations(assetId, address).toList().firstOrNull()?.firstOrNull()?.validator
//    }

    private suspend fun update(address: String, delegations: List<DelegationBase>) {
        if (delegations.isNotEmpty()) {
            val baseDelegations = delegations.toRecord(address)
            stakeDao.update(baseDelegations)
        } else {
            stakeDao.deleteBaseDelegation(address)
        }
    }

    suspend fun update(validators: List<DelegationValidator>) {
        stakeDao.updateValidators(validators.toRecord())
    }
}