package com.gemwallet.android.blockchain.clients.polkadot.models

import kotlinx.serialization.Serializable

@Serializable
data class PolkadotTransactionBroadcast(
    val hash: String?,
    val error: String?,
    val cause: String?,
)
