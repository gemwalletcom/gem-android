package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.services.mapper.toApp
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetBasic
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetProperties
import com.wallet.core.primitives.AssetScore
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import uniffi.gemstone.GemGateway

class TokenService(
    private val gateway: GemGateway,
) {
    suspend fun search(query: String) = withContext(Dispatchers.IO) {
        Chain.entries.map {
            async {
                try {
                    if (gateway.getIsTokenAddress(it.string, query)) {
                        getTokenData(AssetId(it, query))
                    } else {
                        null
                    }
                } catch (_: Throwable) {
                    null
                }
            }
        }
        .awaitAll()
        .filterNotNull()
        .map {
            AssetBasic(
                asset = it,
                score = AssetScore(0),
                properties = AssetProperties(
                    isEnabled = false,
                    isBuyable = false,
                    isSellable = false,
                    isSwapable = false,
                    isStakeable = false
                )
            )
        }
    }

    suspend fun getTokenData(assetId: AssetId): Asset? {
        val tokenId = assetId.tokenId ?: return null
        val chain = assetId.chain
        return try {
            if (gateway.getIsTokenAddress(chain.string, tokenId)) {
                val result = gateway.getTokenData(chain.string, tokenId)
                result.toApp()
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }

    }
}