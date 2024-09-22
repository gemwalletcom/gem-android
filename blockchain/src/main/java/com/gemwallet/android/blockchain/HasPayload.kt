package com.gemwallet.android.blockchain

import com.wallet.core.primitives.Chain

fun Chain.memo() = when (this) {
    Chain.Bitcoin,
    Chain.Litecoin,
    Chain.Doge,
    Chain.Ethereum,
    Chain.SmartChain,
    Chain.Polygon,
    Chain.Tron,
    Chain.Optimism,
    Chain.AvalancheC,
    Chain.Base,
    Chain.Aptos,
    Chain.Sui,
    Chain.OpBNB,
    Chain.Fantom,
    Chain.Gnosis,
    Chain.Manta,
    Chain.Blast,
    Chain.ZkSync,
    Chain.Linea,
    Chain.Mantle,
    Chain.Celo,
    Chain.Near,
    Chain.Arbitrum -> PayloadType.None
    Chain.Cosmos,
    Chain.Osmosis,
    Chain.Sei,
    Chain.Thorchain,
    Chain.Celestia,
    Chain.Injective,
    Chain.Xrp,
    Chain.Noble,
    Chain.Solana,
    Chain.Ton -> PayloadType.Memo
}

enum class PayloadType {
    None,
    Memo,
}