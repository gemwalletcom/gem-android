package com.gemwallet.android.blockchain.operators

interface LoadPhraseOperator {
    suspend operator fun invoke(walletId: String, password: String): String
}