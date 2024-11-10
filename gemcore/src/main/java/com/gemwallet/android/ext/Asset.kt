package com.gemwallet.android.ext

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.StakeChain

fun Asset.isStackable(): Boolean {
    return id.type() == AssetSubtype.NATIVE && StakeChain.isStaked(id.chain) && id.chain != Chain.Tron
}

fun Asset.same(other: Asset) = id.same(other.id)