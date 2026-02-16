package com.gemwallet.android.application.wallet.coordinators

interface ToggleWalletPin {
    suspend fun toggleWalletPin(walletId: String)
}