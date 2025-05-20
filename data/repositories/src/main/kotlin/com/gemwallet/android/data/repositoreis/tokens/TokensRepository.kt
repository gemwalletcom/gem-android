package com.gemwallet.android.data.repositoreis.tokens

import com.gemwallet.android.blockchain.clients.GetTokenClient
import com.gemwallet.android.cases.tokens.SearchTokensCase
import com.gemwallet.android.data.service.store.database.AssetsDao
import com.gemwallet.android.data.service.store.database.AssetsPriorityDao
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.data.service.store.database.entities.toRecordPriority
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.wallet.core.primitives.AssetBasic
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetProperties
import com.wallet.core.primitives.AssetScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class TokensRepository (
    private val assetsDao: AssetsDao,
    private val assetsPriorityDao: AssetsPriorityDao,
    private val gemApiClient: GemApiClient,
    private val getTokenClients: List<GetTokenClient>,
) : SearchTokensCase {

    override suspend fun search(query: String): Boolean = withContext(Dispatchers.IO) {
        if (query.isEmpty()) {
            return@withContext false
        }
        val result = gemApiClient.search(query)
        val tokens = result.getOrNull()
        val assets = if (tokens.isNullOrEmpty()) {
            val assets = getTokenClients.map {
                async {
                    try {
                        if (it.isTokenQuery(query)) {
                            it.getTokenData(query)
                        } else {
                            null
                        }
                    } catch (_: Throwable) {
                        null
                    }
                }
            }
            .awaitAll()
            .mapNotNull { it }
            .map { AssetBasic(asset = it, score = AssetScore(0), properties = AssetProperties(false, false, false, false, false)) }
            runCatching { assetsDao.insert(assets.map { it.toRecord() }) }
            assets
        } else {
            runCatching { assetsDao.insert(tokens.map { it.toRecord() }) }
            assetsPriorityDao.put(tokens.toRecordPriority(query))
            tokens
        }
        assets.isNotEmpty()
    }

    override suspend fun search(assetId: AssetId): Boolean {
        val tokenId = assetId.tokenId ?: return false
        val asset = getTokenClients
            .firstOrNull { it.supported(assetId.chain) && it.isTokenQuery(tokenId) }
            ?.getTokenData(tokenId)
        if (asset == null) {
            return search(tokenId)
        }
        val record = AssetBasic(asset = asset, score = AssetScore(0), properties = AssetProperties(false, false, false, false, false))
            .toRecord()
        runCatching { assetsDao.insert(record) }
        return true
    }
}