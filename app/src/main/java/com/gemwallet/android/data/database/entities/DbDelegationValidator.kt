package com.gemwallet.android.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.Chain

@Entity(tableName = "stake_delegation_validator")
data class DbDelegationValidator(
    @PrimaryKey val id: String,
    val chain: Chain,
    val name: String,
    @ColumnInfo("is_active") val isActive: Boolean,
    val commission: Double,
    val apr: Double,
)