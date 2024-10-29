package com.gemwallet.android.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.WalletType

@Entity(tableName = "wallets")
data class DbWallet(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "domain_name") val domainName: String?,
    val type: WalletType,
    val position: Int,
    val pinned: Boolean,
    val index: Int,
)