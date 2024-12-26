package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain

interface TransactionStatusClient : BlockchainClient {

    suspend fun getStatus(request: TransactionStateRequest): Result<TransactionChages>
}

class TransactionStateRequest(
    val chain: Chain,
    val hash: String,
    val block: String,
    val sender: String,
)