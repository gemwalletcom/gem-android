package com.gemwallet.android.blockchain.clients.algorand

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class AlgorandChainData(
    val sequence: ULong,
    val block: String,
    val chainId: String,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Algorand(
            sequence = sequence,
            blockHash = block,
            chainId = chainId,
        )
    }
}

fun GemTransactionLoadMetadata.Algorand.toChainData(): AlgorandChainData {
    return AlgorandChainData(
        sequence = sequence,
        block = blockHash,
        chainId = chainId,
    )
}