package com.gemwallet.android.cases.device

import com.wallet.core.primitives.Wallet

interface SwitchPushEnabled {
    suspend fun switchPushEnabled(enabled: Boolean, wallets: List<Wallet>)
}