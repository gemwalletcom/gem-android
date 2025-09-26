package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class XrpChainData(
    val sequence: Int,
    val blockNumber: Int,
) : ChainSignData

fun GemTransactionLoadMetadata.Xrp.toChainData(): XrpChainData {
    return XrpChainData(
        sequence = sequence.toInt(),
        blockNumber = blockNumber.toInt(),
    )
}