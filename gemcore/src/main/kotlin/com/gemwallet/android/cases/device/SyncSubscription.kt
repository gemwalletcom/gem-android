package com.gemwallet.android.cases.device

import com.wallet.core.primitives.Wallet

interface SyncSubscription {
    suspend fun syncSubscription(wallets: List<Wallet>, added: Boolean = false)
}