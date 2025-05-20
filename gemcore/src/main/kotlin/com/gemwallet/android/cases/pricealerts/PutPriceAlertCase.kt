package com.gemwallet.android.cases.pricealerts

import com.wallet.core.primitives.PriceAlert

interface PutPriceAlertCase {
    suspend fun putPriceAlert(alert: PriceAlert)
}