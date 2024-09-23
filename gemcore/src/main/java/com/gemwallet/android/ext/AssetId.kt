package com.gemwallet.android.ext

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain

fun AssetId.toIdentifier() = "${chain.string}${if (tokenId.isNullOrEmpty()) "" else "_${tokenId}"}"

fun AssetId.type() = if (tokenId.isNullOrEmpty()) AssetSubtype.NATIVE else AssetSubtype.TOKEN

fun String.toAssetId(): AssetId? {
    val components = split("_", limit = 2)
    val chainId = components.firstOrNull() ?: return null
    val chain = Chain.entries.firstOrNull { it.string == chainId } ?: return null
    val token = if (components.size > 1) components[1] else null
    return AssetId(chain, token)
}

fun AssetId.isSwapable() = type() == AssetSubtype.NATIVE
        && (EVMChain.entries.map { it.string }.contains(chain.string) || chain == Chain.Solana)

fun AssetId.same(other: AssetId) = chain == other.chain && tokenId == other.tokenId