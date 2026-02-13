package com.gemwallet.android.application.wallet.coordinators

import com.gemwallet.android.domains.wallet.aggregates.WalletDetailsAggregate
import kotlinx.coroutines.flow.Flow

interface GetWalletDetails {
    fun getWallet(walletId: String): Flow<WalletDetailsAggregate?>
}