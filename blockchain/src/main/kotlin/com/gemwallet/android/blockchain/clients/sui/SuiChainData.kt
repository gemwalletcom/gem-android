package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class SuiChainData(
    val messageBytes: String,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Sui(
            messageBytes = messageBytes
        )
    }
}

fun GemTransactionLoadMetadata.Sui.toChainData(): SuiChainData {
    return SuiChainData(
        messageBytes = messageBytes,
    )
}