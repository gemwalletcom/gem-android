package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain

class HyperCoreGetTokenClient(
    private val chain: Chain,
) : GetTokenClient {
    override suspend fun getTokenData(tokenId: String): Asset? {
        TODO("Not yet implemented")
    }

    override suspend fun isTokenQuery(query: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}