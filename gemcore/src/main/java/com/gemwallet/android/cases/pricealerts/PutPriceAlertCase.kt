package com.gemwallet.android.cases.pricealerts

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.PriceAlert
import kotlinx.coroutines.flow.Flow

interface PutPriceAlertCase {
    suspend fun putPriceAlert(alert: PriceAlert)
}