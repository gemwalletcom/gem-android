package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationState
import java.util.UUID

@Entity(tableName = "stake_delegation_base")
data class DbDelegationBase(
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

fun DelegationBase.toRecord(address: String): DbDelegationBase {
    return DbDelegationBase(
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

fun List<DelegationBase>.toRecord(address: String) = map { it.toRecord(address) }