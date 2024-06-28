package com.gemwallet.android.features.asset.details.models

sealed interface AssetInfoUIState {
    class Idle(val sync: SyncState = SyncState.None) : AssetInfoUIState

    object Loading: AssetInfoUIState

    class Error() : AssetInfoUIState

    class Fatal(val error: AssetStateError) : AssetInfoUIState

    enum class SyncState {
        None,
        Wait,
        Loading,
    }
}

sealed interface AssetStateError {
    data object AssetNotFound : AssetStateError
}