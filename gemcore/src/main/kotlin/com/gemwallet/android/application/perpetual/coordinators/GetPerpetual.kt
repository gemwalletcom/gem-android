package com.gemwallet.android.application.perpetual.coordinators

import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDetailsAggregate
import kotlinx.coroutines.flow.Flow

interface GetPerpetual {
    fun getPerpetual(perpetualId: String): Flow<PerpetualDetailsAggregate>
}