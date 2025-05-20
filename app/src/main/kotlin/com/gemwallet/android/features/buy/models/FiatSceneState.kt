package com.gemwallet.android.features.buy.models

sealed interface FiatSceneState {
    data object Loading : FiatSceneState
    data class Error(val error: BuyError?) : FiatSceneState
}