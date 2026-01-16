package com.gemwallet.android.application.pricealerts.coordinators

import com.gemwallet.android.domains.pricealerts.values.PriceAlertsStateEvent
import kotlinx.coroutines.flow.Flow

interface PriceAlertsStateCoordinator {
    val priceAlertState: Flow<PriceAlertsStateEvent?>

    fun changePriceAlertState(state: PriceAlertsStateEvent)
}