package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "balances", primaryKeys = ["asset_id", "owner"])
data class DbBalance(
    @ColumnInfo("asset_id") val assetId: String,
    val owner: String,

    var available: String = "0",
    @ColumnInfo("available_amount") var availableAmount: Double = 0.0,

    var frozen: String = "0",
    @ColumnInfo("frozen_amount") var frozenAmount: Double = 0.0,

    var locked: String = "0",
    @ColumnInfo("locked_amount") var lockedAmount: Double = 0.0,

    var staked: String = "0",
    @ColumnInfo("staked_amount") var stakedAmount: Double = 0.0,

    var pending: String = "0",
    @ColumnInfo("pending_amount") var pendingAmount: Double = 0.0,

    var rewards: String = "0",
    @ColumnInfo("rewards_amount") var rewardsAmount: Double = 0.0,

    var reserved: String = "0",
    @ColumnInfo("reserved_amount") var reservedAmount: Double = 0.0,

    @ColumnInfo("total_amount") var totalAmount: Double = 0.0,

    var enabled: Boolean = true,
    var hidden: Boolean = false,
    var pinned: Boolean = false,

    @ColumnInfo("updated_at") var updatedAt: Long?
)