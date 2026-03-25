package com.gemwallet.android.application.assets.coordinators

import com.gemwallet.android.domains.wallet.aggregates.WalletSummaryAggregate
import kotlinx.coroutines.flow.Flow

interface GetWalletSummary {
    fun getWalletSummary(): Flow<WalletSummaryAggregate?>
}