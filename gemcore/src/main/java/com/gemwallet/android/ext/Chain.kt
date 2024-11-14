package com.gemwallet.android.ext

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import java.math.BigInteger

val tokenAvailableChains = listOf( // TODO: Redisign
    Chain.AvalancheC,
    Chain.Base,
    Chain.SmartChain,
    Chain.Arbitrum,
    Chain.Polygon,
    Chain.OpBNB,
    Chain.Fantom,
    Chain.Gnosis,
    Chain.Optimism,
    Chain.Manta,
    Chain.Blast,
    Chain.ZkSync,
    Chain.Linea,
    Chain.Mantle,
    Chain.Celo,
    Chain.Ethereum,
    Chain.Tron,
    Chain.Solana,
    Chain.Sui,
    Chain.Ton,
)

fun Chain.getReserveBalance(): BigInteger = when (this) {
    Chain.Xrp -> BigInteger.valueOf(10_000_000)
    else -> BigInteger.ZERO
}

fun Chain.eip1559Support() = when (this) {
    Chain.OpBNB,
    Chain.Optimism,
    Chain.Base,
    Chain.AvalancheC,
    Chain.SmartChain,
    Chain.Polygon,
    Chain.Fantom,
    Chain.Gnosis,
    Chain.Manta,
    Chain.Blast,
    Chain.ZkSync,
    Chain.Linea,
    Chain.Mantle,
    Chain.Celo,
    Chain.World,
    Chain.Ethereum -> true
    Chain.Bitcoin,
    Chain.Litecoin,
    Chain.Solana,
    Chain.Thorchain,
    Chain.Cosmos,
    Chain.Osmosis,
    Chain.Sei,
    Chain.Arbitrum,
    Chain.Ton,
    Chain.Tron,
    Chain.Doge,
    Chain.Aptos,
    Chain.Sui,
    Chain.Celestia,
    Chain.Injective,
    Chain.Noble,
    Chain.Near,
    Chain.Xrp -> false
}

fun Chain.asset(): Asset {
    val wrapper = uniffi.gemstone.assetWrapper(string)
    return Asset(
        id = wrapper.id.toAssetId() ?: throw IllegalArgumentException(),
        name = wrapper.name,
        symbol = wrapper.symbol,
        decimals = wrapper.decimals,
        type = AssetType.entries.firstOrNull { string == wrapper.assetType } ?: AssetType.NATIVE
    )
}

fun Chain.Companion.findByString(value: String): Chain? {
    return Chain.entries.firstOrNull{ it.string == value}
}

fun Chain.Companion.exclude() = setOf(Chain.Celo)

fun Chain.Companion.available() = (Chain.entries.toSet() - exclude())

fun List<Chain>.filter(query: String): List<Chain> {
    return filter {
        val asset =  it.asset()
        asset.symbol.lowercase().startsWith(query) ||
        asset.name.lowercase().startsWith(query) ||
        it.string.lowercase().startsWith(query)
    }
}