package com.gemwallet.android.cases.swap

import com.wallet.core.primitives.Asset
import uniffi.gemstone.SwapperQuote

interface GetSwapQuotes {
    suspend fun getQuotes(
        ownerAddress: String,
        destination: String,
        from: Asset,
        to: Asset,
        amount: String,
        useMaxAmount: Boolean,
    ): List<SwapperQuote>?
}