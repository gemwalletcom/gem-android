package com.gemwallet.features.swap.viewmodels.models

sealed interface SwapState {
    data object None : SwapState
    data object GetQuote : SwapState
    data object Ready : SwapState
    data object Swapping : SwapState
    data object CheckAllowance : SwapState
    data object Approving : SwapState

    class Error(val error: SwapError) : SwapState {
        companion object
    }
}

fun SwapState.Error.Companion.create(err: Throwable) = SwapState.Error(SwapError.toError(err))