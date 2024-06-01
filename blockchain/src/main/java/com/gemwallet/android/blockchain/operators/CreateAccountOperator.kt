package com.gemwallet.android.blockchain.operators

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain

interface CreateAccountOperator {
    operator fun invoke(data: String, chain: Chain): Account
}