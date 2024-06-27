package com.gemwallet.android.features.asset.details.models

sealed interface AssetInfoUIState {
    class Idle(val syncing: Boolean = false) : AssetInfoUIState

    object Loading: AssetInfoUIState

    class Error() : AssetInfoUIState

    class Fatal(val error: AssetStateError) : AssetInfoUIState
}

sealed interface AssetStateError {
    data object AssetNotFound : AssetStateError
}