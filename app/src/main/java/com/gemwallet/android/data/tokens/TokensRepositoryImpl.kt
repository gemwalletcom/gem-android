package com.gemwallet.android.data.tokens

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetFull
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetScore
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TokensRepositoryImpl(
    private val localSource: TokensLocalSource,
    private val gemApiClient: GemApiClient,
    private val getTokenClients: List<GetTokenClient>
) : TokensRepository {

    override suspend fun getByIds(ids: List<AssetId>): List<Asset> = withContext(Dispatchers.IO) {
        localSource.getByIds(ids)
    }

    override suspend fun getByChains(chains: List<Chain>): List<Asset> = withContext(Dispatchers.IO) {
        localSource.getByChains(chains)
    }

    override suspend fun search(query: String) = withContext(Dispatchers.IO) {
        if (query.isEmpty()) {
            return@withContext
        }
        val result = gemApiClient.search(query)
        val tokens = result.getOrNull()
        if (tokens.isNullOrEmpty()) {
            val assets = getTokenClients.map {
                async {
                    try {
                        if (it.isTokenQuery(query)) {
                            it.getTokenData(query)
                        } else {
                            null
                        }
                    } catch (err: Throwable) {
                        null
                    }

                }
            }.awaitAll().mapNotNull { it }.map {
                AssetFull(
                    asset = it,
                    score = AssetScore(0),
                )
            }
            localSource.addTokens(assets)
        } else {
            localSource.addTokens(tokens.filter { it.asset.id != null })
        }
    }

    override suspend fun search(assetId: AssetId) {
        val tokenId = assetId.tokenId ?: return
        val asset = getTokenClients
            .firstOrNull { it.isMaintain(assetId.chain) && it.isTokenQuery(tokenId) }
            ?.getTokenData(tokenId)
        if (asset == null) {
            search(tokenId)
            return
        }
        localSource.addTokens(listOf(AssetFull(asset, score = AssetScore(0))))
    }

    override suspend fun search(chains: List<Chain>, query: String): Flow<List<Asset>> {
        return localSource.search(chains = chains, query)
    }

    override suspend fun assembleAssetInfo(assetId: AssetId): AssetInfo? {
        return localSource.assembleAssetInfo(assetId)
    }
}