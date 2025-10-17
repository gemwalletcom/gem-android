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
    StakeChain.Tron,
    StakeChain.Ethereum,
    StakeChain.Aptos,
    StakeChain.HyperCore -> false
}

fun StakeChain.withdraw(): Boolean = when (this) {
    StakeChain.Cosmos,
    StakeChain.Injective,
    StakeChain.Sei,
    StakeChain.Celestia,
    StakeChain.Osmosis,
    StakeChain.SmartChain,
    StakeChain.Ethereum,
    StakeChain.Tron,
    StakeChain.HyperCore,
    StakeChain.Aptos,
    StakeChain.Sui-> false
    StakeChain.Solana -> true
}

fun StakeChain.claimed(): Boolean = when (this) {
    StakeChain.Cosmos,
    StakeChain.Injective,
    StakeChain.Sei,
    StakeChain.Celestia,
    StakeChain.Osmosis,
    StakeChain.Tron -> true
    StakeChain.Solana,
    StakeChain.Sui,
    StakeChain.SmartChain,
    StakeChain.Ethereum,
    StakeChain.Aptos,
    StakeChain.HyperCore -> false
}

fun StakeChain.freezed(): Boolean = when (this) {
    StakeChain.Tron -> true
    StakeChain.Cosmos,
    StakeChain.Injective,
    StakeChain.Sei,
    StakeChain.Celestia,
    StakeChain.Osmosis,
    StakeChain.Solana,
    StakeChain.Sui,
    StakeChain.SmartChain,
    StakeChain.Ethereum,
    StakeChain.Aptos,
    StakeChain.HyperCore -> false
}