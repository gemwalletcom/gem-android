package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.TransactionChanges
import com.wallet.core.primitives.Chain

interface TransactionStatusClient : BlockchainClient {

    suspend fun getStatus(request: TransactionStateRequest): TransactionChanges
}

class TransactionStateRequest(
    val chain: Chain,
    val hash: String,
    val block: String,
    val sender: String,
)

sealed class TransactionStatusError(message: String? = null) : Exception(message)

object ServiceUnavailable : TransactionStatusError()