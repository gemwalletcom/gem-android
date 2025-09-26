package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class PolkadotChainData(
    val sequence: ULong,
    val genesisHash: String,
    val blockHash: String,
    val blockNumber: ULong,
    val specVersion: ULong,
    val transactionVersion: ULong,
    val period: Long,
) : ChainSignData

fun GemTransactionLoadMetadata.Polkadot.toChainData(): PolkadotChainData {
    return PolkadotChainData(
        sequence = sequence,
        genesisHash = genesisHash,
        blockHash = blockHash,
        blockNumber = blockNumber,
        specVersion = specVersion,
        transactionVersion = transactionVersion,
        period = period.toLong(),
    )
}