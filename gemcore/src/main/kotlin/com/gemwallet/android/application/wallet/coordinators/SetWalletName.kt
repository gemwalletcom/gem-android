package com.gemwallet.android.application.wallet.coordinators

interface SetWalletName {
    suspend fun setWalletName(walletId: String, name: String)
}