package com.gemwallet.android.blockchain.clients.stellar

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class StellarChainData(
    val sequence: ULong,
    val isDestinationAddressExist: Boolean,
) : ChainSignData {
    companion object {
        const val tokenAccountCreation = "tokenAccountCreation"
    }
}

fun GemTransactionLoadMetadata.Stellar.toChainData(): StellarChainData {
    return StellarChainData(
        sequence = sequence,
        isDestinationAddressExist = isDestinationAddressExist,
    )
}