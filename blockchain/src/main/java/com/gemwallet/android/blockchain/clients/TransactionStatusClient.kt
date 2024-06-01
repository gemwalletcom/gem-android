package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.TransactionChages

interface TransactionStatusClient : BlockchainClient {

    suspend fun getStatus(owner: String, txId: String): Result<TransactionChages>
}