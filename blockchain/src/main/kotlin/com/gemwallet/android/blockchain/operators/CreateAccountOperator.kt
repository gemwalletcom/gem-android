package com.gemwallet.android.blockchain.operators

import com.wallet.core.primitives.Account
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

interface CreateAccountOperator {
    operator fun invoke(walletType: WalletType, data: String, chain: Chain): Account
}