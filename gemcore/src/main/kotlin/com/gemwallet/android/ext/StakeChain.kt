package com.gemwallet.android.ext

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.StakeChain
import uniffi.gemstone.ChainConfig
import uniffi.gemstone.Config
import uniffi.gemstone.StakeChainConfig

fun StakeChain.Companion.isStaked(chain: Chain): Boolean = byChain(chain) != null

fun StakeChain.Companion.byChain(chain: Chain): StakeChain?
    = StakeChain.entries.firstOrNull { it.string == chain.string }

val Chain.claimed: Boolean
    get() = Config().getStakeConfig(string).canClaimRewards

val Chain.withdraw: Boolean
    get() = Config().getStakeConfig(string).canWithdraw

val Chain.redelegated: Boolean
    get() = Config().getStakeConfig(string).canRedelegate

val Chain.changeAmountOnUnstake: Boolean
    get() = Config().getStakeConfig(string).changeAmountOnUnstake

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
    StakeChain.Monad,
    StakeChain.HyperCore -> false
}