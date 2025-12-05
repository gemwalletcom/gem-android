package com.gemwallet.android.application.perpetual.coordinators

import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDetailsDataAggregate
import kotlinx.coroutines.flow.Flow

interface GetPerpetual {
    fun getPerpetual(perpetualId: String): Flow<PerpetualDetailsDataAggregate>
}