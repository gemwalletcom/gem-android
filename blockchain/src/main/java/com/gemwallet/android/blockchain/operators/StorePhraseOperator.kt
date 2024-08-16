package com.gemwallet.android.blockchain.operators

import com.wallet.core.primitives.Wallet

interface StorePhraseOperator {
    suspend operator fun invoke(wallet: Wallet, data: String, password: String): Result<Boolean>
}