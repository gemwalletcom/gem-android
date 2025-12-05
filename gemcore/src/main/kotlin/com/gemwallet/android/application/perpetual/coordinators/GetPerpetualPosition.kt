package com.gemwallet.android.application.perpetual.coordinators

import com.gemwallet.android.domains.perpetual.aggregates.PerpetualPositionDetailsDataAggregate
import kotlinx.coroutines.flow.Flow

interface GetPerpetualPosition {
    fun getPerpetualPosition(id: String): Flow<PerpetualPositionDetailsDataAggregate>
}