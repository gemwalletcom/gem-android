package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class StellarChainData(
    val sequence: ULong,
    val isDestinationAddressExist: Boolean,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Stellar(
            sequence = sequence,
            isDestinationAddressExist = isDestinationAddressExist,
        )
    }
}

fun GemTransactionLoadMetadata.Stellar.toChainData(): StellarChainData {
    return StellarChainData(
        sequence = sequence,
        isDestinationAddressExist = isDestinationAddressExist,
    )
}