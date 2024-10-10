package com.gemwallet.android.data.database.entities

import androidx.room.Entity
import com.wallet.core.primitives.AssetType

@Entity(tableName = "tokens", primaryKeys = ["id"])
data class DbToken(
    val id: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val type: AssetType,
    val rank: Int,
)