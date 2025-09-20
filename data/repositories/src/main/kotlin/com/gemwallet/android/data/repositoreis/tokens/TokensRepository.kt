package com.gemwallet.android.data.repositoreis.tokens

import com.gemwallet.android.blockchain.services.TokenService
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
import kotlinx.coroutines.withContext

class TokensRepository (
    private val assetsDao: AssetsDao,
    private val assetsPriorityDao: AssetsPriorityDao,
    private val gemApiClient: GemApiClient,
    private val tokenService: TokenService,
) : SearchTokensCase {

    override suspend fun search(query: String): Boolean = withContext(Dispatchers.IO) {
        if (query.isEmpty()) {
            return@withContext false
        }
        val tokens = try {
            gemApiClient.search(query)
        } catch (_: Throwable) {
            return@withContext false
        }
        val assets = if (tokens.isEmpty()) {
            val assets = tokenService.search(query)
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
        val asset = tokenService.getTokenData(assetId)
        if (asset == null) {
            return search(tokenId)
        }
        val record = AssetBasic(asset = asset, score = AssetScore(0), properties = AssetProperties(
            isEnabled = false,
            isBuyable = false,
            isSellable = false,
            isSwapable = false,
            isStakeable = false
        ))
        .toRecord()
        runCatching { assetsDao.insert(record) }
        return true
    }
}