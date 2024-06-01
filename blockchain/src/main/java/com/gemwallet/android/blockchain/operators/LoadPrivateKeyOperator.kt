package com.gemwallet.android.blockchain.operators

import com.wallet.core.primitives.Chain

interface LoadPrivateKeyOperator {
    suspend operator fun invoke(walletId: String, chain: Chain, password: String): ByteArray
}