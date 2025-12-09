package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "stake_delegation_validator")
data class DbDelegationValidator(
    @PrimaryKey val id: String,
    val chain: Chain,
    val name: String,
    @ColumnInfo("is_active") val isActive: Boolean,
    val commission: Double,
    val apr: Double,
)

fun DbDelegationValidator.toDTO(): DelegationValidator {
    return DelegationValidator(
        id = id,
        chain = chain,
        name = name,
        isActive = isActive,
        commission = commission,
        apr = apr,
    )
}

fun DelegationValidator.toRecord(): DbDelegationValidator {
    return DbDelegationValidator(
        id = id,
        chain = chain,
        name = name,
        isActive = isActive,
        commission = commission,
        apr = apr,
    )
}

fun List<DbDelegationValidator>.toDTO() = map { it.toDTO() }

fun Flow<List<DbDelegationValidator>>.toDTO() = map { it.toDTO() }

fun List<DelegationValidator>.toRecord() = map { it.toRecord() }