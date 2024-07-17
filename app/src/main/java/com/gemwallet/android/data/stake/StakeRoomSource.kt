package com.gemwallet.android.data.stake

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator
import com.wallet.core.primitives.Price
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import java.util.UUID

@Entity(tableName = "stake_delegation_validator")
data class RoomDelegationValidator(
    @PrimaryKey val id: String,
    val chain: Chain,
    val name: String,
    @ColumnInfo("is_active") val isActive: Boolean,
    val commission: Double,
    val apr: Double,
)

@Entity(tableName = "stake_delegation_base")
data class RoomDelegationBase(
    @PrimaryKey val id: String,
    val address: String,
    @ColumnInfo("delegation_id") val delegationId: String,
    @ColumnInfo("validator_id") val validatorId: String,
    @ColumnInfo("asset_id") val assetId: String,
    val state: DelegationState,
    val balance: String,
    val rewards: String,
    @ColumnInfo("completion_date") val completionDate: Long? = null,
    val price: Double? = null,
    @ColumnInfo("price_change") val priceChange: Double? = null,
    val shares: String? = null,
)

data class RoomDelegation(
    val validatorId: String,
    val delegationId: String,
    val assetId: String,
    val chain: Chain,
    val name: String,
    val isActive: Boolean,
    val commission: Double,
    val apr: Double,
    val address: String,
    val state: DelegationState,
    val balance: String,
    val rewards: String,
    val completionDate: Long? = null,
    val price: Double? = null,
    val priceChange: Double? = null,
    val shares: String? = null,
) {
    fun toModel(): Delegation? {
        return Delegation(
            validator = DelegationValidator(
                id = validatorId,
                chain = chain,
                name = name,
                isActive = isActive,
                commision = commission,
                apr = apr
            ),
            base = DelegationBase(
                assetId = assetId.toAssetId() ?: return null,
                validatorId = validatorId,
                delegationId = delegationId,
                state = state,
                balance = balance,
                completionDate = completionDate,
                rewards = rewards,
                shares = shares ?: "",
            ),
            price = if (price != null) {
                Price(price, priceChange ?: 0.0)
            } else {
                null
            }
        )
    }
}

@Dao
abstract class StakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateValidators(validators: List<RoomDelegationValidator>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateDelegations(delegations: List<RoomDelegationBase>)

    @Query("DELETE FROM stake_delegation_base WHERE " +
            "asset_id=:assetId " +
            "AND address=:address")
    abstract fun deleteBaseDelegation(assetId: String, address: String)

    @Query("DELETE FROM stake_delegation_base WHERE " +
            "address=:address")
    abstract fun deleteBaseDelegation(address: String)

    @Query("SELECT * FROM stake_delegation_validator WHERE chain=:chain")
    abstract fun getValidators(chain: Chain): Flow<List<RoomDelegationValidator>>

    @Query("SELECT * FROM stake_delegation_validator WHERE " +
            "chain=:chain AND " +
            "is_active=:isActive " +
            "ORDER BY apr DESC")
    abstract fun getStakeValidators(chain: Chain, isActive: Boolean = true): List<RoomDelegationValidator>

    @Query("SELECT * FROM stake_delegation_validator WHERE " +
            "chain=:chain AND " +
            "id=:validatorId " +
            "ORDER BY apr DESC")
    abstract fun getStakeValidator(chain: Chain, validatorId: String): RoomDelegationValidator?

    @Query(
        "SELECT " +
            "validator.id as validatorId," +
            "validator.chain as chain," +
            "validator.name as name," +
            "validator.is_active as isActive," +
            "validator.commission as commission," +
            "validator.apr as apr," +
            "base.address as address," +
            "base.delegation_id as delegationId," +
            "base.asset_id as assetId," +
            "base.state as state," +
            "base.balance as balance," +
            "base.completion_date as completionDate," +
            "base.price as price," +
            "base.price_change as priceChange," +
            "base.rewards as rewards," +
            "base.shares as shares" +
            " " +
        "FROM stake_delegation_base as base " +
        "LEFT JOIN stake_delegation_validator as validator ON base.validator_id=validator.id " +
        "WHERE asset_id=:assetId AND address=:address " +
        "ORDER BY validator.name"
    )
    abstract fun getDelegations(assetId: String, address: String): Flow<List<RoomDelegation>>

    @Query(
        "SELECT " +
                "validator.id as validatorId," +
                "validator.chain as chain," +
                "validator.name as name," +
                "validator.is_active as isActive," +
                "validator.commission as commission," +
                "validator.apr as apr," +
                "base.address as address," +
                "base.delegation_id as delegationId," +
                "base.asset_id as assetId," +
                "base.state as state," +
                "base.balance as balance," +
                "base.completion_date as completionDate," +
                "base.price as price," +
                "base.price_change as priceChange," +
                "base.rewards as rewards," +
                "base.shares as shares" +
                " " +
                "FROM stake_delegation_base as base " +
                "LEFT JOIN stake_delegation_validator as validator ON base.validator_id=validator.id " +
                "WHERE base.delegation_id=:delegationId AND validator.id=:validatorId"
    )
    abstract fun getDelegation(validatorId: String, delegationId: String): Flow<RoomDelegation?>

    @Transaction
    open fun update(
        baseDelegations: List<RoomDelegationBase>,
    ) {
        for (baseDelegation in baseDelegations) {
            deleteBaseDelegation(baseDelegation.assetId, baseDelegation.address)
        }
        updateDelegations(baseDelegations)
    }
}

private fun DelegationValidator.toRoom(): RoomDelegationValidator {
    return RoomDelegationValidator(
        id = id,
        chain = chain,
        name = name,
        isActive = isActive,
        commission = commision,
        apr = apr,
    )
}

private fun DelegationBase.toRoom(address: String): RoomDelegationBase {
    return RoomDelegationBase(
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

fun RoomDelegationValidator.toModel(): DelegationValidator {
    return DelegationValidator(
        id = id,
        chain = chain,
        name = name,
        isActive = isActive,
        commision = commission,
        apr = apr,
    )
}

class StakeRoomSource(
    private val stakeDao: StakeDao,
) : StakeLocalSource {

    override suspend fun update(validators: List<DelegationValidator>) {
        stakeDao.updateValidators(validators.map { it.toRoom() })
    }

    override suspend fun update(address: String, delegations: List<DelegationBase>) {
        if (delegations.isNotEmpty()) {
            val baseDelegations = delegations.map { it.toRoom(address) }
            stakeDao.update(baseDelegations)
        } else {
            stakeDao.deleteBaseDelegation(address)
        }
    }

    override suspend fun getValidators(
        chain: Chain,
    ): Flow<List<DelegationValidator>> {
        return stakeDao.getValidators(chain)
            .map { items ->
                items
                    .map { item -> item.toModel() }
                    .filter { it.isActive }
                    .sortedByDescending { it.apr }
            }
    }

    override suspend fun getDelegations(assetId: AssetId, address: String): Flow<List<Delegation>> {
        return stakeDao.getDelegations(assetId.toIdentifier(), address)
            .map { items -> items.mapNotNull { it.toModel() } }
    }

    override suspend fun getDelegation(validatorId: String, delegationId: String): Flow<Delegation?> {
        return stakeDao.getDelegation(validatorId = validatorId, delegationId = delegationId)
            .map { it?.toModel() }
    }

    override suspend fun getStakeValidator(assetId: AssetId): DelegationValidator? {
        return stakeDao.getStakeValidators(assetId.chain).maxByOrNull { it.apr }?.toModel()
    }

    override suspend fun getStakeValidator(assetId: AssetId, validatorId: String): DelegationValidator? {
        return stakeDao.getStakeValidator(assetId.chain, validatorId)?.toModel()
    }

    override suspend fun getUnstakeValidator(assetId: AssetId, address: String): DelegationValidator? {
        return getDelegations(assetId, address).toList().firstOrNull()?.firstOrNull()?.validator
    }
}