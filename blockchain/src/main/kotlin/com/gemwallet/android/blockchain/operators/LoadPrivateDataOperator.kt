package com.gemwallet.android.blockchain.operators

import com.wallet.core.primitives.Wallet

interface LoadPrivateDataOperator {
    suspend operator fun invoke(wallet: Wallet, password: String): String
}