package com.gemwallet.android.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.WalletConnectionState

@Entity(tableName = "room_connection")
data class DbConnection(
    @PrimaryKey val id: String,
    @ColumnInfo("wallet_id") val walletId: String,
    @ColumnInfo("session_id") val sessionId: String,
    val state: WalletConnectionState,
    @ColumnInfo("created_at") val createdAt: Long,
    @ColumnInfo("expire_at") val expireAt: Long,
    @ColumnInfo("app_name") val appName: String,
    @ColumnInfo("app_description") val appDescription: String,
    @ColumnInfo("app_url") val appUrl: String,
    @ColumnInfo("app_icon") val appIcon: String,
    @ColumnInfo("redirect_native") val redirectNative: String?,
    @ColumnInfo("redirect_universal") val redirectUniversal: String?,
)