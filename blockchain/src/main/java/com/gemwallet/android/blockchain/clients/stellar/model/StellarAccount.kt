package com.gemwallet.android.blockchain.clients.stellar.model

import com.wallet.core.blockchain.stellar.StellarBalance

data class StellarAccount(
    val sequence: String?,
    val balances: List<StellarBalance>?,
    val status: Int?,
)