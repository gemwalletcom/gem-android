package com.gemwallet.android.cases.pricealerts

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency

interface EnablePriceAlert {
    suspend fun setPriceAlertEnabled(enabled: Boolean)

    suspend fun setAssetPriceAlertEnabled(assetId: AssetId, currency: Currency, enabled: Boolean)

    fun isPriceAlertEnabled(): Boolean
}