package com.gemwallet.android.features.swap.models

import com.gemwallet.android.ui.components.image.getSwapProviderIcon
import uniffi.gemstone.SwapProviderType

data class SwapProviderItem(
    val swapProvider: SwapProviderType,
    val price: String? = null,
    val fiat: String? = null,
) {
    val icon: String by lazy { swapProvider.id.getSwapProviderIcon() }
    val name: String by lazy { swapProvider.protocol }
}
