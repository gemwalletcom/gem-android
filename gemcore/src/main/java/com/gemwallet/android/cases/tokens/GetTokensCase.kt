package com.gemwallet.android.cases.tokens

import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import kotlinx.coroutines.flow.Flow

interface GetTokensCase {
    suspend fun getByIds(ids: List<AssetId>): List<Asset>

    fun getByChains(chains: List<Chain>, query: String): Flow<List<Asset>>

    suspend fun assembleAssetInfo(assetId: AssetId): AssetInfo?
}