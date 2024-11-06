package com.gemwallet.android.ext

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain

fun Chain.asset(): Asset {

    val assetId = AssetId(this)
    return when (this) {
        Chain.Bitcoin -> Asset(
            id = assetId,
            name = "Bitcoin",
            symbol = "BTC",
            decimals = 8,
            type = AssetType.NATIVE,
        )
        Chain.Litecoin -> Asset(
            id = assetId,
            name = "Litecoin",
            symbol = "LTC",
            decimals = 8,
            type = AssetType.NATIVE,
        )
        Chain.Ethereum -> Asset(
            id = assetId,
            name = "Ethereum",
            symbol = "ETH",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.SmartChain -> Asset(
            id = assetId,
            name = "Smart Chain",
            symbol = "BNB",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Solana -> Asset(
            id = assetId,
            name = "Solana",
            symbol = "SOL",
            decimals = 9,
            type = AssetType.NATIVE,
        )
        Chain.Polygon -> Asset(
            id = assetId,
            name = "Polygon",
            symbol = "MATIC",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Thorchain -> Asset(
            id = assetId,
            name = "Thorchain",
            symbol = "RUNE",
            decimals = 8,
            type = AssetType.NATIVE,
        )
        Chain.Cosmos -> Asset(
            id = assetId,
            name = "Cosmos",
            symbol = "ATOM",
            decimals = 6,
            type = AssetType.NATIVE,
        )
        Chain.Osmosis -> Asset(
            id = assetId,
            name = "Osmosis",
            symbol = "OSMO",
            decimals = 6,
            type = AssetType.NATIVE,
        )
        Chain.Arbitrum -> Asset(
            id = assetId,
            name = "Arbitrum",
            symbol = "ETH",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Ton -> Asset(
            id = assetId,
            name = "TON",
            symbol = "TON",
            decimals = 9,
            type = AssetType.NATIVE,
        )
        Chain.Tron -> Asset(
            id = assetId,
            name = "TRON",
            symbol = "TRX",
            decimals = 6,
            type = AssetType.NATIVE,
        )
        Chain.Doge -> Asset(
            id = assetId,
            name = "Dogecoin",
            symbol = "DOGE",
            decimals = 8,
            type = AssetType.NATIVE,
        )
        Chain.Optimism -> Asset(
            id = assetId,
            name = "Optimism",
            symbol = "ETH",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Aptos -> Asset(
            id = assetId,
            name = "Aptos",
            symbol = "APT",
            decimals = 8,
            type = AssetType.NATIVE,
        )
        Chain.Base -> Asset(
            id = assetId,
            name = "Base",
            symbol = "ETH",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.AvalancheC -> Asset(
            id = assetId,
            name = "Avalanche",
            symbol = "AVAX",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Sui -> Asset(
            id = assetId,
            name = "Sui",
            symbol = "SUI",
            decimals = 9,
            type = AssetType.NATIVE,
        )
        Chain.Xrp -> Asset(
            id = assetId,
            name = "XRP",
            symbol = "XRP",
            decimals = 6,
            type = AssetType.NATIVE,
        )
        Chain.OpBNB -> Asset(
            id = assetId,
            name = "OpBNB",
            symbol = "BNB",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Fantom -> Asset(
            id = assetId,
            name = "Fantom",
            symbol = "FTM",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Gnosis -> Asset(
            id = assetId,
            name = "Gnosis Chain",
            symbol = "GNO",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Celestia -> Asset(
            id = assetId,
            name = "Celestia",
            symbol = "TIA",
            decimals = 6,
            type = AssetType.NATIVE,
        )
        Chain.Injective -> Asset(
            id = assetId,
            name = "Injective",
            symbol = "INJ",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Sei -> Asset(
            id = assetId,
            name = "Sei",
            symbol = "SEI",
            decimals = 6,
            type = AssetType.NATIVE,
        )
        Chain.Manta -> Asset(
            id = assetId,
            name = "Manta",
            symbol = "ETH",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Blast -> Asset(
            id = assetId,
            name = "Blast",
            symbol = "ETH",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Noble -> Asset(
            id = assetId,
            name = "Noble",
            symbol = "USDC",
            decimals = 6,
            type = AssetType.NATIVE,
        )
        Chain.ZkSync ->  Asset(
            id = assetId,
            name = "zkSync",
            symbol = "ETH",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Linea ->  Asset(
            id = assetId,
            name = "Linea",
            symbol = "ETH",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Mantle ->  Asset(
            id = assetId,
            name = "Mantle",
            symbol = "MNT",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Celo ->  Asset(
            id = assetId,
            name = "Celo",
            symbol = "CELO",
            decimals = 18,
            type = AssetType.NATIVE,
        )
        Chain.Near ->  Asset(
            id = assetId,
            name = "Near",
            symbol = "NEAR",
            decimals = 24,
            type = AssetType.NATIVE,
        )
        Chain.World ->  Asset(
            id = assetId,
            name = "World",
            symbol = "WRLD",
            decimals = 24,
            type = AssetType.NATIVE,
        )
    }
}