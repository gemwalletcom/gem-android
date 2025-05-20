package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.blockchain.clients.aptos.services.AptosTokensService
import com.gemwallet.android.math.has0xPrefix
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain

class AptosGetTokenClient(
    private val chain: Chain,
    private val tokensService: AptosTokensService,
) : GetTokenClient {

    override suspend fun getTokenData(tokenId: String): Asset? {
        val address = tokenId.split("::").firstOrNull() ?: return null
        val resource = "0x1::coin::CoinInfo<$tokenId>"
        val tokenInfo = tokensService.resource(address, resource).getOrNull()?.data ?: return null
        return Asset(
            id = AssetId(chain, tokenId),
            name = tokenInfo.name,
            symbol = tokenInfo.symbol,
            decimals = tokenInfo.decimals,
            type = AssetType.TOKEN
        )
    }

    override suspend fun isTokenQuery(query: String): Boolean = isTokenAddress(query)

    override fun supported(chain: Chain): Boolean = chain == this.chain

    companion object {
        fun isTokenAddress(tokenId: String): Boolean {
            return tokenId.has0xPrefix() && tokenId.contains("::")
        }
    }
}