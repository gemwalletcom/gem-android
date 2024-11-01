package com.gemwallet.android.model

import com.wallet.core.primitives.WalletType

data class WalletSummary(
    val walletId: String,
    val icon: String,
    val name: String,
    val type: WalletType,
    val totalValue: Fiat,
    val changedValue: Fiat,
    val changedPercentages: Double,
)