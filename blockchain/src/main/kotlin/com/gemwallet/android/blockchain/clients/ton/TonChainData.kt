package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class TonChainData(
    val sequence: ULong,
    val jettonAddress: String? = null,
    val expireAt: Int? = null
) : ChainSignData

fun GemTransactionLoadMetadata.Ton.toChainData(): TonChainData {
    return TonChainData(
        sequence = sequence,
        jettonAddress = senderTokenAddress,
    )
}