package com.gemwallet.android.data.bridge

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletConnection
import com.wallet.core.primitives.WalletConnectionSession
import com.wallet.core.primitives.WalletConnectionSessionAppMetadata
import com.wallet.core.primitives.WalletConnectionState

@Entity(tableName = "room_connection")
data class RoomConnection(
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

@Dao
interface ConnectionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(connection: RoomConnection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(connections: List<RoomConnection>)

    @Update
    fun update(connection: RoomConnection)

    @Query("SELECT * FROM room_connection")
    fun getAll(): List<RoomConnection>

    @Query("SELECT * FROM room_connection WHERE session_id = :sessionId")
    fun getBySessionId(sessionId: String): RoomConnection

    @Query("DELETE FROM room_connection WHERE id = :id")
    fun delete(id: String)

    @Query("DELETE FROM room_connection")
    fun deleteAll()
}

class ConnectionsRoomSource(
    private val connectionsDao: ConnectionsDao,
) : ConnectionsLocalSource {
    override suspend fun getAll(wallets: List<Wallet>): List<WalletConnection> {
        return connectionsDao.getAll().mapNotNull { room ->
            val wallet = wallets.firstOrNull { it.id ==  room.walletId } ?: return@mapNotNull null
            WalletConnection(
                wallet = wallet,
                session = WalletConnectionSession(
                    id = room.id,
                    sessionId = room.sessionId,
                    state = room.state,
                    createdAt = room.createdAt,
                    expireAt = room.expireAt,
                    chains = emptyList(),
                    metadata = WalletConnectionSessionAppMetadata(
                        name = room.appName,
                        description = room.appDescription,
                        icon = room.appIcon,
                        url = room.appUrl,
                        redirectNative = room.redirectNative,
                        redirectUniversal = room.redirectUniversal,
                    ),
                )
            )
        }
    }

    override suspend fun addConnection(connection: WalletConnection): Result<Boolean> {
        val session = connection.session
        connectionsDao.insert(
            RoomConnection(
                id = session.id,
                walletId = connection.wallet.id,
                sessionId = session.sessionId,
                state = session.state,
                createdAt = session.createdAt,
                expireAt = session.expireAt,
                appName = session.metadata.name,
                appDescription = session.metadata.description,
                appUrl = session.metadata.url,
                appIcon = session.metadata.icon,
                redirectNative = session.metadata.redirectNative,
                redirectUniversal = session.metadata.redirectUniversal,
            )
        )
        return Result.success(true)
    }

    override suspend fun updateConnection(session: WalletConnectionSession) {
        val room = connectionsDao.getBySessionId(session.sessionId)
        connectionsDao.update(
            room.copy(
                expireAt = session.expireAt,
                appName = session.metadata.name,
                appDescription = session.metadata.description,
                appUrl = session.metadata.url,
                appIcon = session.metadata.icon,
                redirectNative = session.metadata.redirectNative,
                redirectUniversal = session.metadata.redirectUniversal,
            )
        )
    }

    override suspend fun deleteConnection(id: String): Result<Boolean> {
        connectionsDao.delete(id)
        return Result.success(true)
    }

    override suspend fun deleteAllConnections(): Result<Boolean> {
        connectionsDao.deleteAll()
        return Result.success(true)
    }
}