package com.gemwallet.features.swap.viewmodels.models

import com.gemwallet.android.domains.asset.getSwapProviderIcon
import uniffi.gemstone.SwapperProviderType

data class SwapProviderItem(
    val swapProvider: SwapperProviderType,
    val price: String? = null,
    val fiat: String? = null,
) {
    val icon: String by lazy { swapProvider.id.getSwapProviderIcon() }
    val name: String by lazy { swapProvider.protocol }
}
