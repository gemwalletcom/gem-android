package com.gemwallet.android.cases.device

import com.wallet.core.primitives.Wallet

interface SyncSubscriptionCase {
    suspend fun syncSubscription(wallets: List<Wallet>)
}