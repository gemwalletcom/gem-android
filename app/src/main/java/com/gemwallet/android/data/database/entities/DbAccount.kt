package com.gemwallet.android.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.wallet.core.primitives.Chain

@Entity(tableName = "accounts", primaryKeys = ["wallet_id", "address", "chain", "derivation_path"])
data class DbAccount(
    @ColumnInfo(name = "wallet_id") val walletId: String,
    @ColumnInfo(name = "derivation_path") val derivationPath: String,
    val address: String,
    val chain: Chain,
    val extendedPublicKey: String?,
)