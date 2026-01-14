package com.gemwallet.android.data.repositoreis.pricealerts

import com.wallet.core.primitives.PriceAlert

interface PriceAlertRepository {

    suspend fun addPriceAlert(priceAlert: PriceAlert)

    suspend fun hasSamePriceAlert(priceAlert: PriceAlert): Boolean
}