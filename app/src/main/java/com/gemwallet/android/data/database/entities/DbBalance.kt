package com.gemwallet.android.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.wallet.core.primitives.BalanceType

@Entity(tableName = "balances", primaryKeys = ["asset_id", "address", "type"])
data class DbBalance(
    @ColumnInfo("asset_id", index = true) val assetId: String,
    @ColumnInfo(index = true) val address: String,
    val type: BalanceType,
    val amount: String,
    @ColumnInfo("updated_at") val updatedAt: Long,
)