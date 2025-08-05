package com.gemwallet.android.features.swap.viewmodels.models

sealed interface SwapState {
    data object None : SwapState
    data object GetQuote : SwapState
    data object Ready : SwapState
    data object Swapping : SwapState
    data object CheckAllowance : SwapState
    data object RequestApprove : SwapState
    data object Approving : SwapState
    data class Error(val error: SwapError) : SwapState
}