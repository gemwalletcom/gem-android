package com.gemwallet.android.domains.percentage

import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.absoluteValue

fun Double?.formatAsPercentage(minimumFractionDigits: Int = 2, isShowSign: Boolean = true, isShowZero: Boolean = true): String {
    val percents = this ?: 0.0
    return if (percents == 0.0 && !isShowZero) {
        ""
    } else {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.DOWN
        df.minimumFractionDigits = minimumFractionDigits
        val formattedValue = df.format(percents.absoluteValue)
        val afterFormat = df.parse(df.format(percents))?.toDouble() ?: 0.0
        return (if (isShowSign) if (afterFormat > 0) "+" else if (afterFormat < 0) "-" else "" else "") +
                "${if (afterFormat == 0.0) if (isShowZero) "0.00" else "" else formattedValue}%"
    }
}