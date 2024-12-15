package com.gemwallet.android.ext

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import uniffi.gemstone.Config
import java.math.BigInteger

fun Chain.assetType(): AssetType? {
    return when (this) {
        Chain.OpBNB,
        Chain.SmartChain -> AssetType.BEP20

        Chain.Tron -> AssetType.TRC20

        Chain.Solana -> AssetType.SPL

        Chain.Ton -> AssetType.JETTON

        Chain.Sui -> AssetType.TOKEN

        Chain.Cosmos,
        Chain.Osmosis,
        Chain.Celestia,
        Chain.Injective,
        Chain.Sei,
        Chain.Noble -> null

        Chain.Ethereum,
        Chain.Polygon,
        Chain.Arbitrum,
        Chain.Optimism,
        Chain.Base,
        Chain.AvalancheC,
        Chain.Fantom,
        Chain.Gnosis,
        Chain.Manta,
        Chain.Blast,
        Chain.ZkSync,
        Chain.Linea,
        Chain.Mantle,
        Chain.World -> AssetType.ERC20

        Chain.Celo,
        Chain.Bitcoin,
        Chain.Litecoin,
        Chain.Doge,
        Chain.Thorchain,
        Chain.Aptos,
        Chain.Xrp,
        Chain.Near -> null
    }
}

fun Chain.getReserveBalance(): BigInteger = when (this) {
    Chain.Xrp -> Config().getChainConfig(this.string).accountActivationFee?.toBigInteger() ?: BigInteger.ZERO
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

fun Chain.toChainType(): ChainType {
    return when (this) {
        Chain.Solana -> ChainType.Solana
        Chain.Ton -> ChainType.Ton
        Chain.Tron -> ChainType.Tron
        Chain.Aptos -> ChainType.Aptos
        Chain.Sui -> ChainType.Sui
        Chain.Xrp -> ChainType.Xrp
        Chain.Near -> ChainType.Near
        Chain.Bitcoin,
        Chain.Doge,
        Chain.Litecoin -> ChainType.Bitcoin
        Chain.Thorchain,
        Chain.Osmosis,
        Chain.Celestia,
        Chain.Injective,
        Chain.Sei,
        Chain.Noble,
        Chain.Cosmos -> ChainType.Cosmos
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
        Chain.World,
        Chain.Ethereum -> ChainType.Ethereum
    }
}


fun Chain.getNetworkId(): String {
    return Config().getChainConfig(string).networkId
}

fun Chain.isSwapSupport(): Boolean {
    return Config().getChainConfig(string).isSwapSupported
}

fun Chain.Companion.swapSupport() = Chain.entries.filter { it.isSwapSupport() }
