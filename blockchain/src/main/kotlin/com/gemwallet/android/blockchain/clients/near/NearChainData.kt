package com.gemwallet.android.blockchain.clients.near

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class NearChainData(
    val block: String,
    val sequence: ULong,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Near(
            blockHash = block,
            sequence = sequence,
        )
    }
}

fun GemTransactionLoadMetadata.Near.toChainData(): NearChainData {
    return NearChainData(
        block = blockHash,
        sequence = sequence
    )
}