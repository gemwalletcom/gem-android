package com.gemwallet.android.cases.device

import com.wallet.core.primitives.Wallet

interface SwitchPushEnabled {
    suspend fun switchPushEnabledCase(enabled: Boolean, wallets: List<Wallet>)
}