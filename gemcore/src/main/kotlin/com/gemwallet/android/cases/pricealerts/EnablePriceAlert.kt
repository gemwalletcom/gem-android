package com.gemwallet.android.cases.pricealerts

import com.wallet.core.primitives.AssetId

interface EnablePriceAlert {
    suspend fun setPriceAlertEnabled(enabled: Boolean)

    suspend fun setAssetPriceAlertEnabled(assetId: AssetId, enabled: Boolean)

    fun isPriceAlertEnabled(): Boolean
}