package com.gemwallet.android.application.perpetual.coordinators

import com.gemwallet.android.domains.perpetual.values.PerpetualBalance
import kotlinx.coroutines.flow.Flow

interface GetPerpetualBalances {
    fun getPerpetualBalance(): Flow<PerpetualBalance>
}