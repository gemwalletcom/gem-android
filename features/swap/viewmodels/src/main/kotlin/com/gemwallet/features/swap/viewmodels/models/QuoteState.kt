package com.gemwallet.features.swap.viewmodels.models

import com.gemwallet.android.domains.asset.calculateFiat
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.features.swap.viewmodels.cases.estimateSwapRate
import uniffi.gemstone.SwapperQuote
import java.math.BigDecimal
import java.text.NumberFormat

data class QuoteState(
    val quote: SwapperQuote,
    val pay: AssetInfo,
    val receive: AssetInfo,
)

internal val QuoteState.formattedToAmount: String
    get() = receive.asset.format(Crypto(quote.toValue), 8, showSymbol = false)

internal fun QuoteState.validate(): SwapState {
    val availableBalance = pay.balance.balance.available.toBigInteger()
    val fromValue = quote.fromValue
    return if (availableBalance < fromValue.toBigInteger()) {
        SwapState.Error(SwapError.InsufficientBalance(pay.asset.symbol))
    } else {
        SwapState.Ready
    }
}

internal val QuoteState.rates: SwapRate?
    get() = estimateSwapRate(pay.asset, receive.asset, quote.fromValue, quote.toValue)

internal val QuoteState.estimateTime: String?
    get() = quote.etaInSeconds?.let { it.toDouble() / 60.0 }?.takeIf { it > 0 }?.let {
        val format = NumberFormat.getInstance()
        format.maximumFractionDigits = 2
        format.format(it)
    }

internal val QuoteState.receiveEquivalent: BigDecimal
    get() = receive.calculateFiat(quote.toValue)

internal val QuoteState.payEquivalent: BigDecimal
    get() = pay.calculateFiat(quote.fromValue)