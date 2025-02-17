package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.blockchain.clients.solana.services.SolanaRpcClient
import com.gemwallet.android.blockchain.rpc.model.JSONRpcRequest
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import wallet.core.jni.Base58

class SolanaTokenClient(
    private val chain: Chain,
    private val rpcClient: SolanaRpcClient,
) : GetTokenClient {

    override suspend fun getTokenData(tokenId: String): Asset? {
        val metadataKey = uniffi.gemstone.solanaDeriveMetadataPda(tokenId)
        val tokenInfo = rpcClient.getAccountInfoSpl(
            JSONRpcRequest(
                SolanaMethod.GetAccountInfo.value,
                params = listOf(
                    tokenId,
                    mapOf(
                        "encoding" to "jsonParsed"
                    ),
                )
            )
        ).getOrNull()?.result?.value?.data?.parsed?.info ?: return null

        val base64 = rpcClient.getAccountInfoMpl(
            JSONRpcRequest(
                SolanaMethod.GetAccountInfo.value,
                params = listOf(
                    metadataKey,
                    mapOf(
                        "encoding" to "jsonParsed"
                    ),
                )
            )
        ).getOrNull()?.result?.value?.data?.first() ?: return null
        val metadata = uniffi.gemstone.solanaDecodeMetadata(base64)
        return Asset(
            id = AssetId(chain = chain, tokenId = tokenId),
            name = metadata.name,
            symbol = metadata.symbol,
            decimals = tokenInfo.decimals,
            type = AssetType.SPL,
        )
    }

    override suspend fun isTokenQuery(query: String): Boolean = isTokenAddress(query)

    override fun supported(chain: Chain): Boolean = this.chain == chain

    companion object {
        fun isTokenAddress(tokenId: String): Boolean {
            return tokenId.length in 40..60 && Base58.decodeNoCheck(tokenId).isNotEmpty()
        }
    }
}