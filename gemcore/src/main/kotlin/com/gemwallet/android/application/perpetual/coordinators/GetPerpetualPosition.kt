package com.gemwallet.android.application.perpetual.coordinators

import com.gemwallet.android.domains.perpetual.aggregates.PerpetualPositionDetailsDataAggregate
import kotlinx.coroutines.flow.Flow

interface GetPerpetualPosition {
    fun getPositionByPerpetual(id: String): Flow<PerpetualPositionDetailsDataAggregate?>
}