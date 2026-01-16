package com.gemwallet.android.application.pricealerts.coordinators

import com.gemwallet.android.domains.pricealerts.aggregates.PriceAlertDataAggregate
import com.wallet.core.primitives.AssetId
import kotlinx.coroutines.flow.Flow

interface GetPriceAlerts {
    fun getPriceAlerts(assetId: AssetId? = null): Flow<List<PriceAlertDataAggregate>>

    fun groupByTargetAndAsset(items: List<PriceAlertDataAggregate>): Map<AssetId?, List<PriceAlertDataAggregate>> {
        val result = mutableMapOf<AssetId?, List<PriceAlertDataAggregate>>()

        val withoutTarget = items.filter { !it.hasTarget }
        val withTarget = (items - withoutTarget.toSet()).groupBy { it.assetId }

        result[null] = withoutTarget
        result.putAll(withTarget)

        return result
    }
}