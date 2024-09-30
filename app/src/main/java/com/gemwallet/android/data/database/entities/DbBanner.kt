package com.gemwallet.android.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.BannerState

@Entity(tableName = "banners", primaryKeys = ["wallet_id", "asset_id"])
data class DbBanner(
    @ColumnInfo("wallet_id") val walletId: String,
    @ColumnInfo("asset_id") val assetId: String,
    val state: BannerState,
    val event: BannerEvent,
)