package com.gemwallet.android.cases.pricealerts

import com.wallet.core.primitives.PriceAlert

interface PutPriceAlert {
    suspend fun putPriceAlert(alert: PriceAlert)
}