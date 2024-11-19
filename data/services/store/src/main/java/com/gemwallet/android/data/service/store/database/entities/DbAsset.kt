package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain

@Entity(tableName = "assets", primaryKeys = ["owner_address", "id"])
data class DbAsset(
    @ColumnInfo("owner_address") val address: String,
    val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
    val chain: Chain,
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