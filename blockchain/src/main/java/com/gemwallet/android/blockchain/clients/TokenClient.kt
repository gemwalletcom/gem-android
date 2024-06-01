package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.AssetId

interface TokenClient : BlockchainClient {
    suspend fun getAccountTokens(account: Account): Result<List<AssetId>>
}