package com.gemwallet.features.swap.viewmodels.cases

import com.gemwallet.android.cases.swap.GetSwapQuotes
import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.availableFormatted
import com.gemwallet.android.model.format
import com.gemwallet.features.swap.viewmodels.models.QuoteRequestParams
import com.gemwallet.features.swap.viewmodels.models.QuotesState
import com.gemwallet.features.swap.viewmodels.models.SwapProviderItem
import com.gemwallet.features.swap.viewmodels.models.SwapRate
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import uniffi.gemstone.SwapperQuote
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext

internal fun refreshMachine(value: String): Flow<Long> {
    try {
        if (value.numberParse() == BigDecimal.ZERO) {
            throw IllegalArgumentException()
        }
    } catch (_: Throwable) {
        return emptyFlow()
    }
    return flow {
        while (true) {
            delay(30 * 1000)
            emit(System.currentTimeMillis())
        }
    }
}

internal fun estimateRate(
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

internal suspend fun QuoteRequestParams.requestQuotes(getSwapQuotes: GetSwapQuotes): QuotesState = try {
    val quotes = getSwapQuotes.getQuotes(
        from = pay.asset,
        to = receive.asset,
        ownerAddress = pay.owner!!.address,
        destination = receive.owner!!.address,
        amount = Crypto(value, pay.asset.decimals).atomicValue.toString(),
    ) ?: emptyList()
    QuotesState(quotes, pay, receive)
} catch (err: Throwable) {
    QuotesState(pay = pay, receive = receive, err = err)
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

val AssetInfo.availableBalance: String
    get() = Crypto(balance.balance.available)
        .value(asset.decimals)
        .stripTrailingZeros().toPlainString()

val AssetInfo.availableBalanceFormatted: String
    get() = balance.availableFormatted(4, dynamicPlace = true)


fun AssetInfo.calculateFiat(rawInput: String): BigDecimal {
    val value = Crypto(rawInput.toBigIntegerOrNull() ?: BigInteger.ZERO)
        .value(asset.decimals)
    return calculateFiat(value)
}

fun AssetInfo.calculateFiat(value: BigDecimal): BigDecimal {
    return price?.takeIf { it.price.price > 0.0 }?.let {
        value * it.price.price.toBigDecimal()
    } ?: return BigDecimal.ZERO
}

fun AssetInfo.formatFiat(value: BigDecimal): String {
    if (value <= BigDecimal.ZERO) {
        return ""
    }

    return price?.currency?.format(value) ?: ""
}