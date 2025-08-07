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
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal

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
        priceValue = price.price.price,
        currency = price.currency,
        asset = receive.asset,
    )
} ?: emptyList()

internal fun QuotesState.getProviders(priceValue: Double, currency: Currency, asset: Asset): List<SwapProviderItem> = items.map { quote ->
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

fun AssetInfo.calculateEquivalent(value: String): BigDecimal {
    val valueNum = try {
        value.numberParse()
    } catch (_: Throwable) {
        BigDecimal.ZERO
    }
    return calculateEquivalent(valueNum)
}

fun AssetInfo.calculateEquivalent(value: BigDecimal): BigDecimal {
    val price = price ?: return BigDecimal.ZERO
    return value * price.price.price.toBigDecimal()
}

fun AssetInfo.formatEquivalent(value: String): String {
    val value = calculateEquivalent(value)
    if (value <= BigDecimal.ZERO) {
        return ""
    }

    return price?.currency?.format(value) ?: ""
}