package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain

interface GetTokenClient : BlockchainClient {
    suspend fun getTokenData(tokenId: String): Asset?

    suspend fun isTokenQuery(query: String): Boolean
}