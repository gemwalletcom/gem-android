package com.gemwallet.android.data.service.store.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prices")
data class DbPrice(
    @PrimaryKey val assetId: String,
    val value: Double,
    val dayChanged: Double,
)