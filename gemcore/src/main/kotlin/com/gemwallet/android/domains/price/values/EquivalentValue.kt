package com.gemwallet.android.domains.price.values

import com.gemwallet.android.domains.percentage.formatAsPercentage
import com.gemwallet.android.domains.price.PriceState
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Currency
import java.math.RoundingMode
import java.text.DecimalFormat

interface EquivalentValue {
    val currency: Currency
    val value: Double?
    val changePercentage: Double?

    val valueFormated: String get() {
        val priceValue = value
        return if (priceValue == null || priceValue.isNaN()) {
            ""
        } else {
            currency.format(priceValue, dynamicPlace = true)
        }
    }

    val changePercentageFormatted: String
        get() = changePercentage.formatAsPercentage()

    val state: PriceState
        get() {
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.DOWN
            val formattedValue = try { df.format(changePercentage) } catch (_: Throwable) { "0" }
            val afterFormat = df.parse(formattedValue)?.toDouble() ?: 0.0
            return when {
                afterFormat > 0 -> PriceState.Up
                afterFormat < 0 -> PriceState.Down
                else -> PriceState.None
            }
        }
}