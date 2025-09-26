package com.gemwallet.android.cases.banners

import com.wallet.core.primitives.Wallet

interface GetWalletOperationsEnabled {
    suspend fun walletOperationsEnabled(wallet: Wallet): Boolean
}