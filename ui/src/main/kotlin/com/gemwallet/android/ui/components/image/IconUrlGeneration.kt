package com.gemwallet.android.ui.components.image

import com.gemwallet.android.ext.type
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FiatProvider
import uniffi.gemstone.GemSwapProvider

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

fun GemSwapProvider.getSwapProviderIcon(): String {
    val iconName = when (this) {
        GemSwapProvider.UNISWAP_V4,
        GemSwapProvider.UNISWAP_V3 -> "uniswap"
        GemSwapProvider.PANCAKESWAP_V3,
        GemSwapProvider.PANCAKESWAP_APTOS_V2 -> "pancakeswap"
        GemSwapProvider.THORCHAIN -> "thorchain"
        GemSwapProvider.ORCA -> "orca"
        GemSwapProvider.JUPITER -> "jupiter"
        GemSwapProvider.ACROSS -> "across"
        GemSwapProvider.OKU -> "oku"
        GemSwapProvider.WAGMI -> "wagmi"
        GemSwapProvider.CETUS_AGGREGATOR, GemSwapProvider.CETUS -> "cetus"
        GemSwapProvider.STONFI_V2 -> "stonfi"
        GemSwapProvider.MAYAN -> "mayan"
        GemSwapProvider.RESERVOIR -> "reservoir"
        GemSwapProvider.SYMBIOSIS -> "symbiosis"
        GemSwapProvider.CHAINFLIP -> "chainflip"
    }
    return "file:///android_asset/swap/${iconName.lowercase()}.svg"
}