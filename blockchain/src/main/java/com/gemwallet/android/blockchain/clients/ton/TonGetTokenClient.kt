package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.blockchain.clients.sui.SuiRpcClient
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain

class TonGetTokenClient(
    val chain: Chain,
    val rpcClient: TonRpcClient,
) : GetTokenClient {
    override suspend fun getTokenData(tokenId: String): Asset? {
        val token = rpcClient.tokenData(tokenId).getOrNull()?.result ?: return null
        val data = token.jetton_content.data
        return Asset(
            id = AssetId(chain, tokenId),
            name = data.name,
            symbol = data.symbol,
            decimals = data.decimals.toInt(),
            type = AssetType.JETTON,
        )
    }

    override suspend fun isTokenQuery(query: String): Boolean = isTokenAddress(query)

    override fun maintainChain(): Chain = chain

    companion object {
        fun isTokenAddress(tokenId: String): Boolean {
            return tokenId.startsWith("EQ") && tokenId.length in 40..60
        }
    }
}