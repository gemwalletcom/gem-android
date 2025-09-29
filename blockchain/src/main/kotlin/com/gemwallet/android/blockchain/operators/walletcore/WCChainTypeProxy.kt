package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.ChainTypeProxy
import com.wallet.core.primitives.Chain
import wallet.core.jni.CoinType

class WCChainTypeProxy : ChainTypeProxy<CoinType> {
    override operator fun invoke(chain: Chain): CoinType = when (chain) {
        Chain.Bitcoin -> CoinType.BITCOIN
        Chain.Litecoin -> CoinType.LITECOIN
        Chain.BitcoinCash -> CoinType.BITCOINCASH
        Chain.Doge -> CoinType.DOGECOIN
        Chain.Ethereum,
        Chain.World,
        Chain.Sonic,
        Chain.Abstract,
        Chain.Berachain,
        Chain.Ink,
        Chain.Unichain,
        Chain.Hyperliquid,
        Chain.Monad,
        Chain.Plasma,
        Chain.Blast -> CoinType.ETHEREUM
        Chain.SmartChain -> CoinType.SMARTCHAIN
        Chain.Solana -> CoinType.SOLANA
        Chain.Polygon -> CoinType.POLYGON
        Chain.Thorchain -> CoinType.THORCHAIN
        Chain.Cosmos -> CoinType.COSMOS
        Chain.Osmosis -> CoinType.OSMOSIS
        Chain.Arbitrum -> CoinType.ARBITRUM
        Chain.Ton -> CoinType.TON
        Chain.Tron -> CoinType.TRON
        Chain.Optimism -> CoinType.OPTIMISM
        Chain.AvalancheC -> CoinType.AVALANCHECCHAIN
        Chain.Base -> CoinType.BASE
        Chain.Aptos -> CoinType.APTOS
        Chain.Sui -> CoinType.SUI
        Chain.Xrp -> CoinType.XRP
        Chain.OpBNB -> CoinType.OPBNB
        Chain.Fantom -> CoinType.FANTOM
        Chain.Gnosis -> CoinType.XDAI
        Chain.Celestia -> CoinType.TIA
        Chain.Injective -> CoinType.NATIVEINJECTIVE
        Chain.Sei -> CoinType.SEI
        Chain.Manta -> CoinType.MANTAPACIFIC
        Chain.Noble -> CoinType.NOBLE
        Chain.ZkSync -> CoinType.ZKSYNC
        Chain.Linea -> CoinType.LINEA
        Chain.Mantle -> CoinType.MANTLE
        Chain.Celo -> CoinType.CELO
        Chain.Near -> CoinType.NEAR
        Chain.Algorand -> CoinType.ALGORAND
        Chain.Stellar -> CoinType.STELLAR
        Chain.Polkadot -> CoinType.POLKADOT
        Chain.Cardano -> CoinType.CARDANO
        Chain.HyperCore -> CoinType.ETHEREUM // TODO: HyperCore
    }
}