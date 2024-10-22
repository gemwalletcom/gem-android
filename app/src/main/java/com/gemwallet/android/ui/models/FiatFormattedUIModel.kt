package com.gemwallet.android.ui.models

import com.gemwallet.android.model.Fiat
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Currency

interface FiatFormattedUIModel {
    val currency: Currency

    val fiat: Fiat?

    val fiatFormatted: String
        get() = fiat?.let { currency.format(it) } ?: ""
}