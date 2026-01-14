package com.gemwallet.android.cases.pricealerts

import com.gemwallet.android.model.PriceAlertInfo
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.PriceAlert
import kotlinx.coroutines.flow.Flow

interface GetPriceAlerts {
    fun getPriceAlert(assetId: AssetId): Flow<PriceAlertInfo?>

    fun isAssetPriceAlertEnabled(assetId: AssetId): Flow<Boolean>
}