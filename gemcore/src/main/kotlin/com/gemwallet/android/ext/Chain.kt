package com.gemwallet.android.ext

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.BitcoinChain
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainType
import com.wallet.core.primitives.FeeUnitType
import uniffi.gemstone.Config
import java.math.BigInteger

fun Chain.assetType(): AssetType? {
    return when (this) {
        Chain.OpBNB,
        Chain.SmartChain -> AssetType.BEP20

        Chain.Tron -> AssetType.TRC20

        Chain.Solana -> AssetType.SPL

        Chain.Ton -> AssetType.JETTON

        Chain.Aptos,
        Chain.Sui -> AssetType.TOKEN

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
        Chain.Sonic,
        Chain.Abstract,
        Chain.Ink,
        Chain.Berachain,
        Chain.Unichain,
        Chain.Hyperliquid,
        Chain.Monad,
        Chain.World -> AssetType.ERC20

        Chain.Cosmos,
        Chain.Osmosis,
        Chain.Celestia,
        Chain.Injective,
        Chain.Sei,
        Chain.Noble,
        Chain.Celo,
        Chain.Bitcoin,
        Chain.BitcoinCash,
        Chain.Litecoin,
        Chain.Doge,
        Chain.Thorchain,
        Chain.Xrp,
        Chain.Algorand,
        Chain.Stellar,
        Chain.Polkadot,
        Chain.Cardano,
        Chain.Near -> null
    }
}

fun Chain.getReserveBalance(): BigInteger = Config().getChainConfig(this.string).accountActivationFee?.toBigInteger() ?: BigInteger.ZERO

fun Chain.getReserveBalanceUrl(): String? = Config().getChainConfig(this.string).accountActivationFeeUrl

fun Chain.isStakeSupported(): Boolean = Config().getChainConfig(this.string).isStakeSupported

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
        Chain.Stellar -> ChainType.Stellar
        Chain.Algorand -> ChainType.Algorand
        Chain.Polkadot -> ChainType.Polkadot
        Chain.Cardano -> ChainType.Cardano
        Chain.Bitcoin,
        Chain.Doge,
        Chain.BitcoinCash,
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
        Chain.Sonic,
        Chain.Abstract,
        Chain.Berachain,
        Chain.Unichain,
        Chain.Ink,
        Chain.Hyperliquid,
        Chain.Monad,
        Chain.Ethereum -> ChainType.Ethereum
    }
}


fun Chain.getNetworkId(): String {
    return Config().getChainConfig(string).networkId
}

fun Chain.isSwapSupport(): Boolean {
    if (this == Chain.Xrp) {
        return true
    }
    return try {
        Config().getChainConfig(string).isSwapSupported
    } catch (_: Throwable) {
        false
    }
}

fun Chain.Companion.swapSupport() = Chain.entries.filter { it.isSwapSupport() }

fun Chain.feeUnitType() = FeeUnitType.entries.firstOrNull {
    it.string == Config().getChainConfig(string).feeUnitType
}

fun Chain.isMemoSupport() = Config().getChainConfig(string).isMemoSupported

fun BitcoinChain.fullAddress(address: String) = when (this) {
    BitcoinChain.BitcoinCash -> if (address.startsWith(Chain.BitcoinCash.string)) {
        address
    } else {
        "${Chain.BitcoinCash.string}:" + address
    }
    else -> address
}