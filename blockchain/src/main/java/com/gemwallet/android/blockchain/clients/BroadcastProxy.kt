package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.TransactionType

class BroadcastProxy(
    private val clients: List<BroadcastClient>,
) {
    suspend fun broadcast(account: Account, message: ByteArray, type: TransactionType): Result<String> {
        return clients.firstOrNull { it.isMaintain(account.chain) }
            ?.send(account, message, type) ?: Result.failure(Exception())
    }
}