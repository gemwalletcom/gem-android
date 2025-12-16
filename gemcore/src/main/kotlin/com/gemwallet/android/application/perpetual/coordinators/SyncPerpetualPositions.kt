package com.gemwallet.android.application.perpetual.coordinators

import com.wallet.core.primitives.Account

interface SyncPerpetualPositions {
    suspend fun syncPerpetualPositions()
}