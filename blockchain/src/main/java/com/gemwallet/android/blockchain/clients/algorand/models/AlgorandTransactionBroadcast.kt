package com.gemwallet.android.blockchain.clients.algorand.models

import kotlinx.serialization.Serializable

@Serializable
data class AlgorandTransactionBroadcast(
    val txId: String?,
    val message: String?,
)
