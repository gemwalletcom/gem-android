package com.gemwallet.android.blockchain.clients.ton

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class TonChainData(
    val sequence: ULong,
    val senderTokenAddress: String? = null,
    val recipientTokenAddress: String? = null,
    val expireAt: Int? = null
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Ton(
            sequence = sequence,
            senderTokenAddress = senderTokenAddress,
            recipientTokenAddress = recipientTokenAddress,
        )
    }
}

fun GemTransactionLoadMetadata.Ton.toChainData(): TonChainData {
    return TonChainData(
        sequence = sequence,
        senderTokenAddress = senderTokenAddress,
        recipientTokenAddress = recipientTokenAddress,
    )
}