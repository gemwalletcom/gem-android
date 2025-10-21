package com.gemwallet.android.cases.wallet

interface DeleteWallet {
    suspend fun deleteWallet(
        currentWalletId: String?,
        walletId: String,
        onBoard: () -> Unit,
        onComplete: () -> Unit,
    )
}