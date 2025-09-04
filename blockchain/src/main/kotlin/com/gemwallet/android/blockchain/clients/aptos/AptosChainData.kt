package com.gemwallet.android.blockchain.clients.aptos

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class AptosChainData(
    val sequence: ULong,
) : ChainSignData

fun GemTransactionLoadMetadata.Aptos.toChainData(): AptosChainData {
    return AptosChainData(
        sequence = sequence,
    )
}