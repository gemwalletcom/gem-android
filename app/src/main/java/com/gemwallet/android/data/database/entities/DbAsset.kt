package com.gemwallet.android.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.wallet.core.primitives.AssetType

@Entity(tableName = "assets", primaryKeys = ["owner_address", "id"])
data class DbAsset(
    @ColumnInfo("owner_address", index = true) val address: String,
    @ColumnInfo(index = true) val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
    @ColumnInfo("is_pinned") val isPinned: Boolean = false,
    @ColumnInfo("is_visible") val isVisible: Boolean = true,
    @ColumnInfo("is_buy_enabled") val isBuyEnabled: Boolean = false,
    @ColumnInfo("is_swap_enabled") val isSwapEnabled: Boolean = false,
    @ColumnInfo("is_stake_enabled") val isStakeEnabled: Boolean = false,
    @ColumnInfo("staking_apr") val stakingApr: Double? = null,
    @ColumnInfo("links") val links: String? = null,
    @ColumnInfo("market") val market: String? = null,
    @ColumnInfo("rank") val rank: Int = 0,
    @ColumnInfo("created_at") val createdAt: Long = 0,
    @ColumnInfo("updated_at") val updatedAt: Long = 0,
)