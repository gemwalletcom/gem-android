package com.gemwallet.android.data.tokens

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetFull
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.flow.Flow

interface TokensLocalSource {
    suspend fun addTokens(tokens: List<AssetFull>)

    suspend fun getByIds(ids: List<AssetId>): List<Asset>

    suspend fun getByChains(chains: List<Chain>): List<Asset>

    suspend fun search(chains: List<Chain>, query: String): Flow<List<Asset>>
}