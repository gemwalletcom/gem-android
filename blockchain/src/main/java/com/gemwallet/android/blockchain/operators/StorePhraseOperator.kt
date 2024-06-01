package com.gemwallet.android.blockchain.operators

interface StorePhraseOperator {
    suspend operator fun invoke(
        walletId: String,
        mnemonic: String,
        password: String): Result<Boolean>
}