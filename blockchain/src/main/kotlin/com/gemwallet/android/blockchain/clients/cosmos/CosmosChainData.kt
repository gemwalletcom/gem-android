package com.gemwallet.android.blockchain.clients.cosmos

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class CosmosChainData(
    val chainId: String,
    val accountNumber: ULong,
    val sequence: ULong,
) : ChainSignData

fun GemTransactionLoadMetadata.Cosmos.toChainData(): CosmosChainData {
    return CosmosChainData(
        chainId = chainId,
        accountNumber = accountNumber,
        sequence = sequence
    )
}