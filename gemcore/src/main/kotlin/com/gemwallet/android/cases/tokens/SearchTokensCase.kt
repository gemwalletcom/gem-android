package com.gemwallet.android.cases.tokens

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetTag
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Currency

interface SearchTokensCase {
    suspend fun search(query: String, currency: Currency, chains: List<Chain> = emptyList(), tags: List<AssetTag> = emptyList()): Boolean

    suspend fun search(assetId: AssetId, currency: Currency): Boolean

    suspend fun search(assetIds: List<AssetId>, currency: Currency): Boolean
}