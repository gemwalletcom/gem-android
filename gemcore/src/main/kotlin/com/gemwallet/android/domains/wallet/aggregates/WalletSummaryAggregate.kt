package com.gemwallet.android.domains.wallet.aggregates

import com.gemwallet.android.domains.price.values.EquivalentValue
import com.wallet.core.primitives.WalletType

interface WalletSummaryAggregate {
    val walletType: WalletType
    val walletName: String
    val walletIcon: Any?
    val walletTotalValue: String
    val changedValue: EquivalentValue?
    val isOperationsAvailable: Boolean
    val isSwapAvailable: Boolean
}