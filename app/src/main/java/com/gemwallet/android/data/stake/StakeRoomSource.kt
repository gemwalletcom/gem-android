package com.gemwallet.android.data.stake

import com.gemwallet.android.data.database.StakeDao
import com.gemwallet.android.data.database.entities.DbDelegationValidator
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import java.util.UUID

class StakeRoomSource(
    private val stakeDao: StakeDao,
) {
    suspend fun update(validators: List<DelegationValidator>) {
        stakeDao.updateValidators(validators.map { it.toRoom() })
    }

    suspend fun update(address: String, delegations: List<DelegationBase>) {
        if (delegations.isNotEmpty()) {
            val baseDelegations = delegations.map { it.toRoom(address) }
            stakeDao.update(baseDelegations)
        } else {
            stakeDao.deleteBaseDelegation(address)
        }
    }

    suspend fun getValidators(
        chain: Chain,
    ): Flow<List<DelegationValidator>> {
        return stakeDao.getValidators(chain)
            .map { items ->
                items.map { item -> item.toModel() }
                    .filter { it.isActive }
                    .sortedByDescending { it.apr }
            }
    }

    fun getDelegations(assetId: AssetId, address: String): Flow<List<Delegation>> {
        return stakeDao.getDelegations(assetId.toIdentifier(), address)
            .map { items -> items.mapNotNull { it.toModel() } }
    }

    fun getDelegation(validatorId: String, delegationId: String): Flow<Delegation?> {
        return stakeDao.getDelegation(validatorId = validatorId, delegationId = delegationId)
            .map { it?.toModel() }
    }

    suspend fun getStakeValidator(assetId: AssetId) = withContext(Dispatchers.IO) {
        stakeDao.getStakeValidators(assetId.chain).maxByOrNull { it.apr }?.toModel()
    }

    suspend fun getStakeValidator(assetId: AssetId, validatorId: String) = withContext(Dispatchers.IO) {
        stakeDao.getStakeValidator(assetId.chain, validatorId)?.toModel()
    }

    suspend fun getUnstakeValidator(assetId: AssetId, address: String) = withContext(Dispatchers.IO) {
        getDelegations(assetId, address).toList().firstOrNull()?.firstOrNull()?.validator
    }

    private fun DelegationValidator.toRoom(): DbDelegationValidator {
        return DbDelegationValidator(
            id = id,
            chain = chain,
            name = name,
            isActive = isActive,
            commission = commision,
            apr = apr,
        )
    }

    private fun DelegationBase.toRoom(address: String): com.gemwallet.android.data.database.entities.DbDelegationBase {
        return com.gemwallet.android.data.database.entities.DbDelegationBase(
            id = UUID.randomUUID().toString(),
            address = address,
            delegationId = delegationId,
            validatorId = validatorId,
            assetId = assetId.toIdentifier(),
            state = state,
            balance = balance,
            completionDate = completionDate,
            rewards = rewards,
            shares = shares,
        )
    }

    fun DbDelegationValidator.toModel(): DelegationValidator {
        return DelegationValidator(
            id = id,
            chain = chain,
            name = name,
            isActive = isActive,
            commision = commission,
            apr = apr,
        )
    }
}