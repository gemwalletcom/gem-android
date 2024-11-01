package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session")
data class DbSession(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo("wallet_id") val walletId: String,
    val currency: String,
)
