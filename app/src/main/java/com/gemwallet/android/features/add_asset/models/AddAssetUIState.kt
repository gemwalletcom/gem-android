package com.gemwallet.android.features.add_asset.models

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Chain

class AddAssetUIState(
    val scene: Scene = Scene.Form,
    val chain: Chain = Chain.Ethereum,
    val networkTitle: String = "",
    val networkIcon: String = "",
    val address: String = "",
    val asset: Asset? = null,
    val isLoading: Boolean = false,
    val error: AddAssetError = AddAssetError.None,
    val chains: List<Chain> = listOf(
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
    ),
) {
    enum class Scene {
        QrScanner,
        Form,
        SelectChain,
    }
}

