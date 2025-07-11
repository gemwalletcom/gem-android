package com.gemwallet.android.blockchain.clients.solana

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.blockchain.clients.solana.services.SolanaAccountsService
import com.gemwallet.android.blockchain.clients.solana.services.createAccountInfoRequest
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import wallet.core.jni.Base58

class SolanaTokenClient(
    private val chain: Chain,
    private val accountsService: SolanaAccountsService,
) : GetTokenClient {

    override suspend fun getTokenData(tokenId: String): Asset? = withContext(Dispatchers.IO) {
        val tokenInfo = try {
            accountsService.getAccountInfoSpl(accountsService.createAccountInfoRequest(tokenId))
                .result.value.data.parsed.info
        } catch (_: Throwable) {
            return@withContext null
        }
        // spl 2022
        if (tokenInfo.extensions != null) {
            val extension = tokenInfo.extensions.firstOrNull { it.extension == "tokenMetadata" }
                ?: throw Exception("no tokenMetadata")
            Asset(
                id = AssetId(chain = chain, tokenId = tokenId),
                name = extension.state.name ?: return@withContext null,
                symbol = extension.state.symbol ?: return@withContext null,
                decimals = tokenInfo.decimals,
                type = AssetType.SPL,
            )
        }

        val metadataKey = try {
            uniffi.gemstone.solanaDeriveMetadataPda(tokenId)
        } catch (_: Throwable) {
            return@withContext null
        }

        val base64Job = async {
            try {
                accountsService.getAccountInfoMpl(accountsService.createAccountInfoRequest(metadataKey))
                    .result.value.data.first()
            } catch (_: Throwable) {
                null
            }

        }
        val base64 = base64Job.await() ?: return@withContext null

        val metadata = try {
            uniffi.gemstone.solanaDecodeMetadata(base64)
        } catch (_: Throwable) {
            return@withContext null
        }

        Asset(
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
            return tokenId.length in 40..60 && Base58.decodeNoCheck(tokenId)?.isNotEmpty() == true
        }
    }
}