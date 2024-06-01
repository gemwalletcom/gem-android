package com.gemwallet.android.model

import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Wallet

data class Session(
    val wallet: Wallet,
    val currency: Currency,
)