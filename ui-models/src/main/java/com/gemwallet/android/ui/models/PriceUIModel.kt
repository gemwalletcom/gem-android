package com.gemwallet.android.ui.models

import java.math.RoundingMode
import java.text.DecimalFormat

interface PriceUIModel : FiatFormattedUIModel,
    PercentageFormattedUIModel {
    val state: PriceState
        get() {
            val df = DecimalFormat("#.##")
            df.roundingMode = RoundingMode.DOWN
            val formattedValue = try { df.format(percentage) } catch (_: Throwable) { "0" }
            val afterFormat = df.parse(formattedValue)?.toDouble() ?: 0.0
            return when {
                afterFormat > 0 -> PriceState.Up
                afterFormat < 0 -> PriceState.Down
                else -> PriceState.None
            }
        }
}