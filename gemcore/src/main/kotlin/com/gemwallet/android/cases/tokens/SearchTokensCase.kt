package com.gemwallet.android.cases.tokens

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain

interface SearchTokensCase {
    suspend fun search(query: String, chains: List<Chain> = emptyList()): Boolean

    suspend fun search(assetId: AssetId): Boolean
}