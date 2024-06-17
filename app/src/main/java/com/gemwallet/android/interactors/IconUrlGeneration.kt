package com.gemwallet.android.interactors

import com.gemwallet.android.ext.type
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FiatProvider

fun Asset.chain(): Chain = id.chain

fun Asset.getIconUrl(): String = id.getIconUrl()

fun Asset.getSupportIconUrl(): String = if (id.type() == AssetSubtype.NATIVE) "" else id.chain.getIconUrl()

fun AssetId.getIconUrl(): String {
    return when {
        tokenId.isNullOrEmpty() -> chain.getIconUrl()
        else -> "https://assets.gemwallet.com/blockchains/${chain.string}/assets/${tokenId}/logo.png"
    }
}

fun Chain.getIconUrl(): String = "file:///android_asset/chains/icons/${string}.png"

fun FiatProvider.getIcon(): String = "file:///android_asset/fiat/${name.lowercase()}.png"