package com.gemwallet.features.swap.viewmodels.models

import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.features.swap.viewmodels.models.SwapError.IncorrectInput
import com.gemwallet.features.swap.viewmodels.models.SwapError.InputAmountTooSmall
import com.gemwallet.features.swap.viewmodels.models.SwapError.NoAvailableProvider
import com.gemwallet.features.swap.viewmodels.models.SwapError.NoQuote
import com.gemwallet.features.swap.viewmodels.models.SwapError.NotSupportedAsset
import com.gemwallet.features.swap.viewmodels.models.SwapError.NotSupportedChain
import com.gemwallet.features.swap.viewmodels.models.SwapError.TransactionError
import com.wallet.core.primitives.Asset
import uniffi.gemstone.SwapperException
import java.math.BigDecimal
import java.math.BigInteger

sealed class SwapError : Throwable() {
    object None : SwapError()
    object NoQuote : SwapError()
    object IncorrectInput : SwapError()
    object NotSupportedChain : SwapError()
    object NotSupportedAsset : SwapError()
    class InputAmountTooSmall(val minAmount: String?) : SwapError() {
        fun getValue(asset: Asset): BigDecimal = try {
            val value = minAmount ?: throw IllegalArgumentException()
            Crypto(BigInteger(value)).value(asset.decimals)
        } catch (_: Throwable) {
            null
        } ?: BigDecimal.ZERO

        fun getFormattedValue(asset: Asset): String = try {
            val value = minAmount ?: throw IllegalArgumentException()
            asset.format(BigInteger(value), 2, dynamicPlace = true)
        } catch (_: Throwable) {
            null
        } ?: ""
    }
    object NoAvailableProvider : SwapError()
    object TransactionError : SwapError()
    data class InsufficientBalance(val symbol: String) : SwapError()
    data class Unknown(val data: String) : SwapError()

    companion object
}

fun SwapError.Companion.toError(err: Throwable) = when (err) {
    is SwapperException.NotSupportedAsset -> NotSupportedAsset
    is SwapperException.NotSupportedChain -> NotSupportedChain
    is SwapperException.ComputeQuoteException,
    is SwapperException.NoQuoteAvailable -> NoQuote
    is SwapperException.InvalidRoute -> IncorrectInput
    is SwapperException.InputAmountException -> InputAmountTooSmall(err.minAmount)
    is SwapperException.NoAvailableProvider -> NoAvailableProvider
    is SwapperException.TransactionException -> TransactionError
    else -> SwapError.Unknown(err.localizedMessage ?: err.message ?: "")
}