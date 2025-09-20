package com.gemwallet.android.blockchain.model

import com.wallet.core.primitives.Chain

class TransactionStateRequest(
    val chain: Chain,
    val hash: String,
    val block: String,
    val sender: String,
)