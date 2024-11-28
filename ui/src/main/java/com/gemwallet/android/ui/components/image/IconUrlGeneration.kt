package com.gemwallet.android.ui.components.image

import com.gemwallet.android.ext.type
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FiatProvider

fun Asset.getIconUrl(): String = id.getIconUrl()

fun Asset.getSupportIconUrl(): String? = id.getSupportIconUrl()

fun AssetId.getIconUrl(): String {
    return when {
        tokenId.isNullOrEmpty() -> chain.getIconUrl()
        else -> "https://assets.gemwallet.com/blockchains/${chain.string}/assets/${tokenId}/logo.png"
    }
}

fun AssetId.getSupportIconUrl(): String? = if (type() == AssetSubtype.NATIVE) null else chain.getIconUrl()

fun Chain.getIconUrl(): String = "file:///android_asset/chains/icons/${string}.png"

fun FiatProvider.getIcon(): String = "file:///android_asset/fiat/${name.lowercase()}.png"

fun Int.getDrawableUri() = "android.resource://com.gemwallet.android/drawable/$this"