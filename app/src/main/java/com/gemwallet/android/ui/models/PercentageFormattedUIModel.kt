package com.gemwallet.android.ui.models

import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.absoluteValue

interface PercentageFormattedUIModel {
    val percentage: Double?

    val percentageFormatted: String
        get() {
            val percents = percentage ?: 0.0
            return if (percents == 0.0 && !percentageShowZero) {
                ""
            } else {
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.DOWN
                df.minimumFractionDigits = 2
                val formattedValue = df.format(percents.absoluteValue)
                val afterFormat = df.parse(df.format(percents))?.toDouble() ?: 0.0
                return (if (percentageShowSign) if (afterFormat > 0) "+" else if (afterFormat < 0) "-" else "" else "") +
                        "${if (afterFormat == 0.0) if (percentageShowZero) "0.00" else "" else formattedValue}%"
            }
        }

    val percentageShowZero: Boolean get() =  true

    val percentageShowSign: Boolean get() =  true
}