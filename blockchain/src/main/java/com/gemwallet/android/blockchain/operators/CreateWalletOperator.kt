package com.gemwallet.android.blockchain.operators

interface CreateWalletOperator {
    suspend operator fun invoke(): Result<String>
}