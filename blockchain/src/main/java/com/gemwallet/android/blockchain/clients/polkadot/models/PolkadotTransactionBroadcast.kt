package com.gemwallet.android.blockchain.clients.polkadot.models

data class PolkadotTransactionBroadcast(
    val hash: String?,
    val error: String?,
    val cause: String?,
)
