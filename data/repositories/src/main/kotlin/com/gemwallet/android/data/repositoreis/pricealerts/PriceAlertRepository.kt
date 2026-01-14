package com.gemwallet.android.data.repositoreis.pricealerts

import com.gemwallet.android.model.PriceAlertInfo
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.PriceAlert
import kotlinx.coroutines.flow.Flow

interface PriceAlertRepository {

    suspend fun addPriceAlert(priceAlert: PriceAlert)

    suspend fun hasSamePriceAlert(priceAlert: PriceAlert): Boolean

    fun getPriceAlerts(assetId: AssetId? = null): Flow<List<PriceAlertInfo>>
}