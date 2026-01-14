package com.gemwallet.android.application.pricealerts.coordinators

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlertDirection

interface AddPriceAlert {

    fun addPriceAlert(assetId: AssetId, currency: Currency, price: Double? = null, percentage: Double? = null, direction: PriceAlertDirection? = null)
}