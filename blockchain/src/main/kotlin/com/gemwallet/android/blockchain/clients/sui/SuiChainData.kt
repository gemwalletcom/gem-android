package com.gemwallet.android.blockchain.clients.sui

import com.gemwallet.android.blockchain.clients.aptos.AptosChainData
import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class SuiChainData(
    val messageBytes: String,
) : ChainSignData

fun GemTransactionLoadMetadata.Sui.toChainData(): SuiChainData {
    return SuiChainData(
        messageBytes = messageBytes,
    )
}