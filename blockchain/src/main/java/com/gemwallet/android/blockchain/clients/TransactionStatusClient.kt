package com.gemwallet.android.blockchain.clients

import com.gemwallet.android.model.TransactionChages
import com.wallet.core.primitives.Chain

interface TransactionStatusClient : BlockchainClient {

    suspend fun getStatus(chain: Chain, owner: String, txId: String): Result<TransactionChages>
}