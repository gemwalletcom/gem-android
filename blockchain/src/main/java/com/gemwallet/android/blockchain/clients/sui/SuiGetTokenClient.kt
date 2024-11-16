package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain

class SuiGetTokenClient(
    private val chain: Chain,
    private val rpcClient: SuiRpcClient,
) : GetTokenClient {
    override suspend fun getTokenData(tokenId: String): Asset? {
        val metadata = rpcClient.getCoinMetadata(JSONRpcRequest.create(SuiMethod.CoinMetadata, listOf(tokenId)))
            .getOrNull()
        val result = if (metadata?.result == null) {
            return null
        } else {
            metadata.result
        }
        return Asset(
            id = AssetId(chain, tokenId),
            name = result.name,
            symbol = result.symbol,
            decimals = result.decimals,
            type = AssetType.TOKEN,
        )
    }

    override suspend fun isTokenQuery(query: String): Boolean = isTokenAddress(query)

    override fun isMaintain(chain: Chain): Boolean = this.chain == chain

    companion object {
        fun isTokenAddress(tokenId: String): Boolean {
            return tokenId.startsWith("0x") && tokenId.length in 66..100
        }
    }
}