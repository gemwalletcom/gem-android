package com.gemwallet.android.data.wallet

import com.wallet.core.primitives.Wallet

interface WalletsLocalSource {
    suspend fun getAll(): Result<List<Wallet>>

    suspend fun addWallet(wallet: Wallet): Result<Wallet>

    suspend fun updateWallet(wallet: Wallet): Result<Wallet>

    suspend fun removeWallet(walletId: String): Result<Boolean>

    suspend fun getWallet(walletId: String): Result<Wallet>

    suspend fun togglePin(walletId: String)
}