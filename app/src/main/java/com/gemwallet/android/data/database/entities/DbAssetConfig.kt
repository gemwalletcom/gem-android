package com.gemwallet.android.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "asset_config", primaryKeys = ["asset_id", "wallet_id"])
data class DbAssetConfig(
    @ColumnInfo("asset_id") val assetId: String,
    @ColumnInfo("wallet_id") val walletId: String,
    @ColumnInfo("is_pinned") val isPinned: Boolean = false,
    @ColumnInfo("is_visible") val isVisible: Boolean = true,
    @ColumnInfo("list_position") val listPosition: Int = 0,
)
