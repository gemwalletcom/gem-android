package com.gemwallet.android.blockchain.operators

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet

interface LoadPrivateKeyOperator {
    suspend operator fun invoke(wallet: Wallet, chain: Chain, password: String): ByteArray
}