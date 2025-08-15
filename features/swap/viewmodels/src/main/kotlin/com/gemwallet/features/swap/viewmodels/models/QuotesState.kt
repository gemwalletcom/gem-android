package com.gemwallet.features.swap.viewmodels.models

import com.gemwallet.android.model.AssetInfo
import uniffi.gemstone.SwapperProvider
import uniffi.gemstone.SwapperQuote

internal data class QuotesState(
    val items: List<SwapperQuote> = emptyList(),
    val pay: AssetInfo,
    val receive: AssetInfo,
    val err: Throwable? = null,
)

internal fun QuotesState.getQuote(provider: SwapperProvider?): SwapperQuote? =
    items.firstOrNull { it.data.provider.id == provider } ?: items.firstOrNull()
