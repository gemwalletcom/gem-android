package com.gemwallet.android.data.bridge

import com.gemwallet.android.data.database.ConnectionsDao
import com.gemwallet.android.data.database.entities.DbConnection
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletConnection
import com.wallet.core.primitives.WalletConnectionSession
import com.wallet.core.primitives.WalletConnectionSessionAppMetadata

class ConnectionsRoomSource(
    private val connectionsDao: ConnectionsDao,
) {
    suspend fun getAll(wallets: List<Wallet>): List<WalletConnection> {
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

    suspend fun addConnection(connection: WalletConnection): Result<Boolean> {
        val session = connection.session
        connectionsDao.insert(
            DbConnection(
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

    suspend fun updateConnection(session: WalletConnectionSession) {
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

    suspend fun deleteConnection(id: String): Result<Boolean> {
        connectionsDao.delete(id)
        return Result.success(true)
    }

    suspend fun deleteAllConnections(): Result<Boolean> {
        connectionsDao.deleteAll()
        return Result.success(true)
    }
}