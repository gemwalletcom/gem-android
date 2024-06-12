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
    val chains: List<Chain> = emptyList()
) {
    enum class Scene {
        QrScanner,
        Form,
        SelectChain,
    }
}

