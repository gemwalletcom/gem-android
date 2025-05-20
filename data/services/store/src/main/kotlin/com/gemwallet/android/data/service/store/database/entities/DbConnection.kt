package com.gemwallet.android.data.service.store.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletConnection
import com.wallet.core.primitives.WalletConnectionSession
import com.wallet.core.primitives.WalletConnectionSessionAppMetadata
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

fun DbConnection.toModel(wallet: Wallet): WalletConnection {
    return WalletConnection(
        wallet = wallet,
        session = WalletConnectionSession(
            id = id,
            sessionId = sessionId,
            state = state,
            createdAt = createdAt,
            expireAt = expireAt,
            chains = emptyList(),
            metadata = WalletConnectionSessionAppMetadata(
                name = appName,
                description = appDescription,
                icon = appIcon,
                url = appUrl,
                redirectNative = redirectNative,
                redirectUniversal = redirectUniversal,
            ),
        )
    )
}

fun WalletConnection.toRecord(): DbConnection {
    return DbConnection(
        id = session.id,
        sessionId = session.id,
        state = session.state,
        createdAt = session.createdAt,
        expireAt = session.expireAt,
        appName = session.metadata.name,
        appDescription = session.metadata.description,
        appIcon = session.metadata.icon,
        appUrl = session.metadata.url,
        redirectNative = session.metadata.redirectNative,
        redirectUniversal = session.metadata.redirectUniversal,
        walletId = wallet.id,
    )
}