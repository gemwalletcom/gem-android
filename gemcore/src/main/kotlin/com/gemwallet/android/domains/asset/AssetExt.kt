package com.gemwallet.android.domains.asset

import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.byChain
import com.gemwallet.android.ext.isMemoSupport
import com.gemwallet.android.ext.isStaked
import com.gemwallet.android.ext.type
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.StakeChain
import uniffi.gemstone.Config

val Asset.chain: Chain
    get() = id.chain

val Asset.isStackable: Boolean
    get() = id.type() == AssetSubtype.NATIVE && StakeChain.isStaked(id.chain) && id.chain != Chain.Tron

val Asset.title: String
    get() = "${id.chain.asset().name} (${symbol})"

val Asset.stakeChain: StakeChain?
    get() = StakeChain.byChain(id.chain)

fun Asset.isMemoSupport() = chain.isMemoSupport()