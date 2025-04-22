package com.gemwallet.android.ui.components.image

import com.gemwallet.android.ext.type
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FiatProvider
import uniffi.gemstone.SwapProvider

fun Int.getDrawableUri() = "android.resource://com.gemwallet.android/drawable/$this"

fun Asset.getIconUrl(): String = id.getIconUrl()

fun Asset.getSupportIconUrl(): String? = id.getSupportIconUrl()

fun AssetId.getIconUrl(): String {
    return when {
        tokenId.isNullOrEmpty() -> chain.getIconUrl()
        else -> "https://assets.gemwallet.com/blockchains/${chain.string}/assets/${tokenId}/logo.png"
    }
}

fun AssetId.getSupportIconUrl(): String? = if (type() == AssetSubtype.NATIVE) null else chain.getIconUrl()

fun Chain.getIconUrl(): String = "file:///android_asset/chains/icons/${string}.svg"

fun FiatProvider.getFiatProviderIcon(): String = "file:///android_asset/fiat/${name.lowercase()}.png"

fun SwapProvider.getSwapProviderIcon(): String {
    val iconName = when (this) {
        SwapProvider.UNISWAP_V4,
        SwapProvider.UNISWAP_V3 -> "uniswap"
        SwapProvider.PANCAKE_SWAP_V3,
        SwapProvider.PANCAKE_SWAP_APTOS_V2 -> "pancakeswap"
        SwapProvider.THORCHAIN -> "thorchain"
        SwapProvider.ORCA -> "orca"
        SwapProvider.JUPITER -> "jupiter"
        SwapProvider.ACROSS -> "across"
        SwapProvider.OKU -> "oku"
        SwapProvider.WAGMI -> "wagmi"
        SwapProvider.CETUS -> "cetus"
        SwapProvider.STON_FI_V2 -> "stonfi"
        SwapProvider.MAYAN -> "mayan"
        SwapProvider.RESERVOIR -> "reservoir"
    }
    return "file:///android_asset/swap/${iconName.lowercase()}.svg"
}