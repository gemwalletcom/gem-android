package com.gemwallet.android.ext

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.StakeChain

fun StakeChain.Companion.isStaked(chain: Chain): Boolean = byChain(chain) != null

fun StakeChain.Companion.byChain(chain: Chain): StakeChain?
    = StakeChain.entries.firstOrNull { it.string == chain.string }

fun StakeChain.lockTime(): Long = when (this) {
    StakeChain.Cosmos,
    StakeChain.Injective,
    StakeChain.Sei,
    StakeChain.Celestia -> 1_814_400
    StakeChain.Osmosis -> 1_036_800
    StakeChain.Solana -> 259200
    StakeChain.Sui -> 86400
    StakeChain.SmartChain -> 0L
}

fun StakeChain.redelegated(): Boolean = when (this) {
    StakeChain.Cosmos,
    StakeChain.Injective,
    StakeChain.Sei,
    StakeChain.Celestia,
    StakeChain.Osmosis -> true
    StakeChain.Solana,
    StakeChain.Sui -> false
    StakeChain.SmartChain -> false
}

fun StakeChain.withdraw(): Boolean = when (this) {
    StakeChain.Cosmos,
    StakeChain.Injective,
    StakeChain.Sei,
    StakeChain.Celestia,
    StakeChain.Osmosis,
    StakeChain.Sui-> false
    StakeChain.Solana -> true
    StakeChain.SmartChain -> false
}

fun StakeChain.claimed(): Boolean = when (this) {
    StakeChain.Cosmos,
    StakeChain.Injective,
    StakeChain.Sei,
    StakeChain.Celestia,
    StakeChain.Osmosis -> true
    StakeChain.Solana,
    StakeChain.Sui -> false
    StakeChain.SmartChain -> false
}