package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.TransactionType

interface BroadcastClient : BlockchainClient {
    suspend fun send(account: Account, signedMessage: ByteArray, type: TransactionType): String
}