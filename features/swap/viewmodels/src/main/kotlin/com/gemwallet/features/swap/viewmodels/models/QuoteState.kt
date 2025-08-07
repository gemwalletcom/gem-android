package com.gemwallet.features.swap.viewmodels.models

import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import kotlinx.coroutines.flow.update
import uniffi.gemstone.SwapperQuote
import java.math.BigDecimal
import java.math.MathContext
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
    get() {
        return try {
            val fromAmount = Crypto(quote.fromValue).value(pay.asset.decimals)
            val toAmount = Crypto(quote.toValue).value(receive.asset.decimals)
            val reverse = BigDecimal.ONE.divide(toAmount / fromAmount, MathContext.DECIMAL128)

            val forwardRate = receive.asset.format(toAmount / fromAmount, 2, dynamicPlace = true)
            val reverseRate = pay.asset.format(reverse, 4, dynamicPlace = true)

            SwapRate(
                forward = "1 ${pay.asset.symbol} \u2248 $forwardRate",
                reverse = "1 ${receive.asset.symbol} \u2248 $reverseRate"
            )
        } catch (_: Throwable) {
            null
        }
    }

internal val QuoteState.estimateTime: String?
    get() = quote.etaInSeconds?.let { it.toDouble() / 60.0 }?.takeIf { it > 0 }?.let {
        val format = NumberFormat.getInstance()
        format.maximumFractionDigits = 2
        format.format(it)
    }

internal val QuoteState.receiveEquivalent: BigDecimal
    get() = receive.price?.takeIf { it.price.price > 0 }?.let {
        Crypto(quote.toValue).convert(receive.asset.decimals, it.price.price).value(0)
    } ?: BigDecimal.ZERO