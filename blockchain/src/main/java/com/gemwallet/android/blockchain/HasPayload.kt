package com.gemwallet.android.blockchain

import com.gemwallet.android.ext.toChainType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType

fun Chain.supportMemo(): Boolean = when (this.toChainType()) {
    ChainType.Cosmos,
    ChainType.Xrp,
    ChainType.Solana,
    ChainType.Algorand,
    ChainType.Stellar,
    ChainType.Ton -> true

    ChainType.Near,
    ChainType.Tron,
    ChainType.Sui,
    ChainType.Aptos,
    ChainType.Ethereum,
    ChainType.Bitcoin -> false
}