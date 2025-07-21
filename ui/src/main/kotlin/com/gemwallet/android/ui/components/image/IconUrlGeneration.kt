package com.gemwallet.android.ui.components.image

import com.gemwallet.android.ext.type
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FiatProvider
import uniffi.gemstone.SwapperProvider

fun Int.getDrawableUri() = "android.resource://com.gemwallet.android/drawable/$this"

fun Chain.getIconUrl(): String = "file:///android_asset/chains/icons/${string}.svg"

fun AssetId.getIconUrl(): String = when {
    tokenId.isNullOrEmpty() -> when (chain) {
        Chain.Optimism,
        Chain.Base,
        Chain.ZkSync,
        Chain.Arbitrum,
        Chain.Abstract,
        Chain.Unichain,
        Chain.Ink,
        Chain.Linea,
        Chain.OpBNB,
        Chain.Blast,
        Chain.World,
        Chain.Manta -> "file:///android_asset/chains/icons/${Chain.Ethereum.string}.svg"
        else -> chain.getIconUrl()
    }
    else -> "https://assets.gemwallet.com/blockchains/${chain.string}/assets/${tokenId}/logo.png"
}

fun AssetId.getSupportIconUrl(): String? = when (type()) {
    AssetSubtype.NATIVE -> when (chain) {
        Chain.Optimism,
        Chain.Base,
        Chain.ZkSync,
        Chain.Arbitrum,
        Chain.Abstract,
        Chain.Unichain,
        Chain.Ink,
        Chain.Linea,
        Chain.OpBNB,
        Chain.Blast,
        Chain.World,
        Chain.Manta -> "file:///android_asset/chains/icons/${chain.string}.svg"
        else -> null
    }
    AssetSubtype.TOKEN -> chain.getIconUrl()
}

fun Asset.getIconUrl(): String = id.getIconUrl()

fun Asset.getSupportIconUrl(): String? = id.getSupportIconUrl()

fun FiatProvider.getFiatProviderIcon(): String = "file:///android_asset/fiat/${name.lowercase()}.png"

fun SwapperProvider.getSwapProviderIcon(): String {
    val iconName = when (this) {
        SwapperProvider.UNISWAP_V4,
        SwapperProvider.UNISWAP_V3 -> "uniswap"
        SwapperProvider.PANCAKESWAP_V3,
        SwapperProvider.PANCAKESWAP_APTOS_V2 -> "pancakeswap"
        SwapperProvider.THORCHAIN -> "thorchain"
        SwapperProvider.ORCA -> "orca"
        SwapperProvider.JUPITER -> "jupiter"
        SwapperProvider.ACROSS -> "across"
        SwapperProvider.OKU -> "oku"
        SwapperProvider.WAGMI -> "wagmi"
        SwapperProvider.CETUS_AGGREGATOR, SwapperProvider.CETUS -> "cetus"
        SwapperProvider.STONFI_V2 -> "stonfi"
        SwapperProvider.MAYAN -> "mayan"
        SwapperProvider.RESERVOIR -> "reservoir"
        SwapperProvider.SYMBIOSIS -> "symbiosis"
        SwapperProvider.CHAINFLIP -> "chainflip"
        SwapperProvider.RELAY -> "relay"
        SwapperProvider.AERODROME -> "aerodrome"
    }
    return "file:///android_asset/swap/${iconName.lowercase()}.svg"
}