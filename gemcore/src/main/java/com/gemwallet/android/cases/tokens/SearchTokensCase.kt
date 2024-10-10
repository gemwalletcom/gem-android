package com.gemwallet.android.cases.tokens

import com.wallet.core.primitives.AssetId

interface SearchTokensCase {
    suspend fun search(query: String)

    suspend fun search(assetId: AssetId)
}