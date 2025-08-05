package com.gemwallet.features.swap.viewmodels.models

sealed interface SwapError {
    object None : SwapError
    object NoQuote : SwapError
    object IncorrectInput : SwapError
    object NotSupportedChain : SwapError
    object NotSupportedPair : SwapError
    object NotSupportedAsset : SwapError
    object NotImplemented : SwapError
    object NetworkError : SwapError
    data class InsufficientBalance(val symbol: String) : SwapError
    data class Unknown(val message: String) : SwapError
}