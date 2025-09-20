package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata

data class TronChainData(
    val blockNumber: ULong,
    val blockVersion: ULong,
    val txTrieRoot: String,
    val witnessAddress: String,
    val parentHash: String,
    val blockTimestamp: ULong,
    val votes: Map<String, ULong> = emptyMap()
) : ChainSignData

fun GemTransactionLoadMetadata.Tron.toChainData(): TronChainData {
    return TronChainData(
        blockNumber = blockNumber,
        blockVersion = blockVersion,
        blockTimestamp = blockTimestamp,
        txTrieRoot = transactionTreeRoot,
        witnessAddress = witnessAddress,
        parentHash = parentHash,
        votes = votes
    )
}