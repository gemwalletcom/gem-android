package com.gemwallet.android.model

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

data class ImportType(
    val walletType: WalletType,
    val chain: Chain? = null,
)