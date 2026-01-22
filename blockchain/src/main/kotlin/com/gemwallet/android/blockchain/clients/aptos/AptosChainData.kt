package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class AptosChainData(
    val sequence: ULong,
    val data: String?,
) : ChainSignData

fun GemTransactionLoadMetadata.Aptos.toChainData(): AptosChainData {
    return AptosChainData(
        sequence = sequence,
        data = data,
    )
}

fun AptosChainData.toGem() = GemTransactionLoadMetadata.Aptos(
    sequence = sequence,
    data = data,
)