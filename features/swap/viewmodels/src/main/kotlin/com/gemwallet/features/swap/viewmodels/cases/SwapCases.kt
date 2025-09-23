package com.gemwallet.features.swap.viewmodels.cases

import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.format
import com.gemwallet.features.swap.viewmodels.models.PriceImpact
import com.gemwallet.features.swap.viewmodels.models.PriceImpactType
import com.gemwallet.features.swap.viewmodels.models.QuoteState
import com.gemwallet.features.swap.viewmodels.models.QuotesState
import com.gemwallet.features.swap.viewmodels.models.SlippageModel
import com.gemwallet.features.swap.viewmodels.models.SwapProviderItem
import com.gemwallet.features.swap.viewmodels.models.SwapRate
import com.gemwallet.features.swap.viewmodels.models.payEquivalent
import com.gemwallet.features.swap.viewmodels.models.receiveEquivalent
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import uniffi.gemstone.Config
import uniffi.gemstone.SwapperQuote
import java.math.BigDecimal
import java.math.MathContext
import kotlin.math.absoluteValue

internal fun tickerFlow(value: BigDecimal): Flow<Long> {
    return if (value == BigDecimal.ZERO) {
        emptyFlow()
    } else {
        flow {
            while (true) {
                delay(30 * 1000)
                emit(System.currentTimeMillis())
            }
        }
    }
}

internal fun estimateSwapRate(
    pay: Asset,
    receive: Asset,
    payValue: String,
    receiveValue: String,
): SwapRate? {
    return try {
        val fromAmount = Crypto(payValue).value(pay.decimals)
        val toAmount = Crypto(receiveValue).value(receive.decimals)
        val reverse = BigDecimal.ONE.divide(toAmount / fromAmount, MathContext.DECIMAL128)

        val forwardRate = receive.format(toAmount / fromAmount, 2, dynamicPlace = true)
        val reverseRate = pay.format(reverse, 4, dynamicPlace = true)

        SwapRate(
            forward = "1 ${pay.symbol} \u2248 $forwardRate",
            reverse = "1 ${receive.symbol} \u2248 $reverseRate"
        )
    } catch (_: Throwable) {
        null
    }
}

internal fun QuotesState.getProviders(): List<SwapProviderItem> = receive.price?.let { price ->
    getProviders(
        items,
        priceValue = price.price.price,
        currency = price.currency,
        asset = receive.asset,
    )
} ?: emptyList()

internal fun getProviders(items: List<SwapperQuote>, priceValue: Double, currency: Currency, asset: Asset): List<SwapProviderItem> = items.map { quote ->
    val toValue = Crypto(quote.toValue)
    val fiatValue = toValue.convert(asset.decimals, priceValue)
    val fiatFormatted = currency.format(fiatValue.value(0))
    SwapProviderItem(
        swapProvider = quote.data.provider,
        price = asset.format(toValue, 2, dynamicPlace = true),
        fiat = fiatFormatted,
    )
}

internal fun calculatePriceImpact(quote: QuoteState): PriceImpact? = calculatePriceImpact(
    quote.payEquivalent,
    quote.receiveEquivalent,
)

internal fun getSlippage(quote: QuoteState): SlippageModel? {
    return SlippageModel(quote.quote.data.slippageBps.toDouble() / 100.0)
}

internal fun calculatePriceImpact(pay: BigDecimal, receive: BigDecimal): PriceImpact? {
    return calculatePriceImpactCore(pay, receive) { impact ->
        val isHigh = impact.absoluteValue > Config().getSwapConfig().highPriceImpactPercent.toDouble()
        isHigh
    }
}

internal fun calculatePriceImpactCore(
    pay: BigDecimal, 
    receive: BigDecimal, 
    isHighProvider: (Double) -> Boolean = { false }
): PriceImpact? {
    if (pay.compareTo(BigDecimal.ZERO) == 0 || receive.compareTo(BigDecimal.ZERO) == 0) {
        return null
    }
    val impact = (((receive.toDouble() / pay.toDouble()) - 1.0) * 100)
    val isHigh = isHighProvider(impact)

    return when {
        impact * -1 < 0 -> PriceImpact(impact, PriceImpactType.Positive, isHigh)
        impact * -1 < 1 -> null
        impact * -1 < 5 -> PriceImpact(impact, PriceImpactType.Medium, isHigh)
        else -> PriceImpact(impact, PriceImpactType.High, isHigh)
    }
}