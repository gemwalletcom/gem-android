package com.gemwallet.android.cases.pricealerts

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.PriceAlert
import kotlinx.coroutines.flow.Flow

interface GetPriceAlertsCase {
    fun getPriceAlerts(): Flow<List<PriceAlert>>

    fun getPriceAlert(assetId: AssetId): Flow<PriceAlert?>

    fun isAssetPriceAlertEnabled(assetId: AssetId): Flow<Boolean>
}