package com.gemwallet.android.ext

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.StakeChain

fun StakeChain.Companion.isStaked(chain: Chain): Boolean = byChain(chain) != null

fun StakeChain.Companion.byChain(chain: Chain): StakeChain?
    = StakeChain.entries.firstOrNull { it.string == chain.string }

fun StakeChain.redelegated(): Boolean = when (this) {
    StakeChain.Cosmos,
    StakeChain.Injective,
    StakeChain.Sei,
    StakeChain.Celestia,
    StakeChain.Osmosis -> true
    StakeChain.Solana,
    StakeChain.Sui,
    StakeChain.SmartChain,
    StakeChain.Tron -> false
}

fun StakeChain.withdraw(): Boolean = when (this) {
    StakeChain.Cosmos,
    StakeChain.Injective,
    StakeChain.Sei,
    StakeChain.Celestia,
    StakeChain.Osmosis,
    StakeChain.SmartChain,
    StakeChain.Tron,
    StakeChain.Sui-> false
    StakeChain.Solana -> true
}

fun StakeChain.claimed(): Boolean = when (this) {
    StakeChain.Cosmos,
    StakeChain.Injective,
    StakeChain.Sei,
    StakeChain.Celestia,
    StakeChain.Osmosis -> true
    StakeChain.Solana,
    StakeChain.Sui,
    StakeChain.SmartChain,
    StakeChain.Tron -> false
}