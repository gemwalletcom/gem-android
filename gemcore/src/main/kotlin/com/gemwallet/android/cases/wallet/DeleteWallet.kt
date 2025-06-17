package com.gemwallet.android.cases.wallet

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DeleteWallet {
    suspend fun deleteWallet(walletId: String, onBoard: () -> Unit, onComplete: () -> Unit)
}