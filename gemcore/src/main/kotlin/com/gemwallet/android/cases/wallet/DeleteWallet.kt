package com.gemwallet.android.cases.wallet

interface DeleteWallet {
    suspend fun deleteWallet(walletId: String, onBoard: () -> Unit, onComplete: () -> Unit)
}