package com.gemwallet.android.application.wallet.coordinators

interface SetCurrentWallet {
    suspend fun setCurrentWallet(walletId: String)
}