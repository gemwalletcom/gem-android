package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class XrpChainData(
    val sequence: ULong,
    val blockNumber: ULong,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Xrp(
            sequence = sequence,
            blockNumber = blockNumber,
        )
    }
}

fun GemTransactionLoadMetadata.Xrp.toChainData(): XrpChainData {
    return XrpChainData(
        sequence = sequence,
        blockNumber = blockNumber,
    )
}