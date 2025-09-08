package com.gemwallet.features.buy.viewmodels.models

sealed interface FiatSceneState {
    data object Loading : FiatSceneState
    data class Error(val error: BuyError?) : FiatSceneState
}