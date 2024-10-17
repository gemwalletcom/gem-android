package com.gemwallet.android.ui.models

import com.gemwallet.android.model.format
import com.wallet.core.primitives.Currency
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.absoluteValue

interface PriceUIModel : UIModel {
    val price: Double?
    val dayChange: Double?
    val currency: Currency

    val priceFormatted: String
        get() {
            return price?.let { currency.format(it, decimalPlace = 2, dynamicPlace = true) } ?: ""
        }

    val dayChangeFormatted: String
        get() {
            val dayChange = dayChange ?: 0.0
            return if (dayChange == 0.0/* && !showZero*/) {
                ""
            } else {
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.DOWN
                df.minimumFractionDigits = 2
                val formattedValue = df.format(dayChange.absoluteValue)
                val afterFormat = df.parse(df.format(dayChange))?.toDouble() ?: 0.0
                return formattedValue
//                (if (showSign) if (afterFormat > 0) "+" else if (afterFormat < 0) "-" else "" else "") +
//                        "${if (afterFormat == 0.0) if (showZero) "0.00" else "" else formattedValue}%"
            }
        }
}