package com.gemwallet.android.data.bridge

import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletConnection
import com.wallet.core.primitives.WalletConnectionSession

interface ConnectionsLocalSource {
    suspend fun getAll(wallets: List<Wallet>): List<WalletConnection>

    suspend fun addConnection(connection: WalletConnection): Result<Boolean>

    suspend fun deleteConnection(id: String): Result<Boolean>

    suspend fun deleteAllConnections(): Result<Boolean>

    suspend fun updateConnection(session: WalletConnectionSession)
}