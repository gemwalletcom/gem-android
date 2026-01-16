package com.gemwallet.android.application.pricealerts.coordinators

interface ExcludePriceAlert {

    suspend fun excludePriceAlert(priceAlertId: Int)
}