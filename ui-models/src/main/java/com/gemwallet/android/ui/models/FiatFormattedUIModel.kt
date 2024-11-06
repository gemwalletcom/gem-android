package com.gemwallet.android.ui.models

import com.gemwallet.android.model.format
import java.math.BigDecimal
import kotlin.let

interface FiatFormattedUIModel {
    val currency: com.wallet.core.primitives.Currency

    val fiat: Double?

    val fiatFormatted: String
        get() = fiat?.let { currency.format(it) } ?: ""
}