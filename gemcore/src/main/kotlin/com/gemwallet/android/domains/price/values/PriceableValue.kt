package com.gemwallet.android.domains.price.values

import com.gemwallet.android.domains.percentage.formatAsPercentage
import com.gemwallet.android.domains.price.PriceState
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Currency
import java.math.RoundingMode
import java.text.DecimalFormat

interface PriceableValue {
    val currency: Currency
    val priceValue: Double?
    val dayChangePercentage: Double?

    val priceValueFormated: String get() {
        val priceValue = priceValue
        return if (priceValue == null || priceValue.isNaN()) {
            ""
        } else {
            currency.format(priceValue, dynamicPlace = true)
        }
    }

    val dayChangePercentageFormatted: String
        get() = dayChangePercentage.formatAsPercentage()

    val state: PriceState
        get() {
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.DOWN
            val formattedValue = try { df.format(dayChangePercentage) } catch (_: Throwable) { "0" }
            val afterFormat = df.parse(formattedValue)?.toDouble() ?: 0.0
            return when {
                afterFormat > 0 -> PriceState.Up
                afterFormat < 0 -> PriceState.Down
                else -> PriceState.None
            }
        }
}