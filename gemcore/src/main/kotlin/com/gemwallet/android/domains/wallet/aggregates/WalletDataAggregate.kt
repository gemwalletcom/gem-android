package com.gemwallet.android.domains.wallet.aggregates

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType

interface WalletDataAggregate {
    val id: String
    val isCurrent: Boolean
    val name: String
    val type: WalletType
    val walletAddress: String?
    val walletChain: Chain?
    val isPinned: Boolean
}