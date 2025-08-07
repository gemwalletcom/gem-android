package com.gemwallet.features.swap.viewmodels.models

import com.gemwallet.features.swap.viewmodels.models.SwapError.IncorrectInput
import com.gemwallet.features.swap.viewmodels.models.SwapError.InputAmountTooSmall
import com.gemwallet.features.swap.viewmodels.models.SwapError.NetworkError
import com.gemwallet.features.swap.viewmodels.models.SwapError.NoAvailableProvider
import com.gemwallet.features.swap.viewmodels.models.SwapError.NoQuote
import com.gemwallet.features.swap.viewmodels.models.SwapError.NotImplemented
import com.gemwallet.features.swap.viewmodels.models.SwapError.NotSupportedAsset
import com.gemwallet.features.swap.viewmodels.models.SwapError.NotSupportedChain
import com.gemwallet.features.swap.viewmodels.models.SwapError.NotSupportedPair
import com.gemwallet.features.swap.viewmodels.models.SwapError.TransactionError
import uniffi.gemstone.SwapperException

sealed interface SwapError {
    object None : SwapError
    object NoQuote : SwapError
    object IncorrectInput : SwapError
    object NotSupportedChain : SwapError
    object NotSupportedPair : SwapError
    object NotSupportedAsset : SwapError
    object NotImplemented : SwapError
    object NetworkError : SwapError
    object InputAmountTooSmall : SwapError
    object NoAvailableProvider : SwapError
    object TransactionError : SwapError
    data class InsufficientBalance(val symbol: String) : SwapError
    data class Unknown(val message: String) : SwapError

    companion object
}

fun SwapError.Companion.toError(err: Throwable) = when (err) {
    is SwapperException.NotSupportedAsset -> NotSupportedAsset
    is SwapperException.NotSupportedPair -> NotSupportedPair
    is SwapperException.NotSupportedChain -> NotSupportedChain
    is SwapperException.ComputeQuoteException,
    is SwapperException.NoQuoteAvailable -> NoQuote
    is SwapperException.NotImplemented -> NotImplemented
    is SwapperException.NetworkException -> NetworkError
    is SwapperException.AbiException -> NetworkError
    is SwapperException.InvalidAddress,
    is SwapperException.InvalidRoute,
    is SwapperException.InvalidAmount  -> IncorrectInput
    is SwapperException.InputAmountTooSmall -> InputAmountTooSmall
    is SwapperException.NoAvailableProvider -> NoAvailableProvider
    is SwapperException.TransactionException -> TransactionError
    else -> SwapError.Unknown(err.localizedMessage ?: err.message ?: "")
}