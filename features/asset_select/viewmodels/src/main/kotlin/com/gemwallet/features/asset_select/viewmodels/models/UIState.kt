package com.gemwallet.features.asset_select.viewmodels.models

sealed interface UIState {
    data object Idle : UIState
    data object Empty : UIState
    data object Loading : UIState
}