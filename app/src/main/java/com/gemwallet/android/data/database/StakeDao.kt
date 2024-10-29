package com.gemwallet.android.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.gemwallet.android.data.database.entities.RoomDelegation
import com.gemwallet.android.data.database.entities.DbDelegationBase
import com.gemwallet.android.data.database.entities.DbDelegationValidator
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.flow.Flow

@Dao
abstract class StakeDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    abstract suspend fun updateValidators(validators: List<DbDelegationValidator>)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    abstract suspend fun updateDelegations(delegations: List<DbDelegationBase>)

    @Query("DELETE FROM stake_delegation_base WHERE " +
            "asset_id=:assetId " +
            "AND address=:address")
    abstract suspend fun deleteBaseDelegation(assetId: String, address: String)

    @Query("DELETE FROM stake_delegation_base WHERE " +
            "address=:address")
    abstract suspend fun deleteBaseDelegation(address: String)

    @Query("SELECT * FROM stake_delegation_validator WHERE chain=:chain")
    abstract fun getValidators(chain: Chain): Flow<List<DbDelegationValidator>>

    @Query("SELECT * FROM stake_delegation_validator WHERE " +
            "chain=:chain AND " +
            "is_active=:isActive " +
            "ORDER BY apr DESC")
    abstract suspend fun getStakeValidators(chain: Chain, isActive: Boolean = true): List<DbDelegationValidator>

    @Query("SELECT * FROM stake_delegation_validator WHERE " +
            "chain=:chain AND " +
            "id=:validatorId " +
            "ORDER BY apr DESC")
    abstract suspend fun getStakeValidator(chain: Chain, validatorId: String): DbDelegationValidator?

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
    open suspend fun update(
        baseDelegations: List<DbDelegationBase>,
    ) {
        for (baseDelegation in baseDelegations) {
            deleteBaseDelegation(baseDelegation.assetId, baseDelegation.address)
        }
        updateDelegations(baseDelegations)
    }
}