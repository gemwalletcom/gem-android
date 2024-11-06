package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class DbPrice(
    @PrimaryKey @ColumnInfo("asset_id") val assetId: String,
    val value: Double? = 0.0,
    @ColumnInfo("day_changed") val dayChanged: Double? = 0.0,
    val currency: String,
)