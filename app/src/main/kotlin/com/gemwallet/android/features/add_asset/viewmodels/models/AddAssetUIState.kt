package com.gemwallet.android.features.add_asset.viewmodels.models

class AddAssetUIState(
    val scene: Scene = Scene.Form,
    val onSelectChain: (() -> Unit)? = null,
) {
    enum class Scene {
        QrScanner,
        Form,
        SelectChain,
    }
}

sealed interface TokenSearchState {
    object Idle : TokenSearchState
    object Loading : TokenSearchState
    class Error(val message: AddAssetError) : TokenSearchState
}