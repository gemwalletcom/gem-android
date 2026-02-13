package com.gemwallet.android.application.wallet.coordinators

interface DeleteWallet {
    suspend fun deleteWallet(
        walletId: String,
        onBoard: () -> Unit,
        onComplete: () -> Unit,
    )
}