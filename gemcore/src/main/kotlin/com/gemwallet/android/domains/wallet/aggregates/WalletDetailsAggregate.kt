package com.gemwallet.android.domains.wallet.aggregates

import com.wallet.core.primitives.WalletType

interface WalletDetailsAggregate {
    val id: String
    val name: String
    val type: WalletType
    val addresses: List<String>
}