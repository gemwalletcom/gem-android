package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemResource
import uniffi.gemstone.GemTransactionLoadMetadata
import uniffi.gemstone.TronStakeData

data class TronChainData(
    val blockNumber: ULong,
    val blockVersion: ULong,
    val txTrieRoot: String,
    val witnessAddress: String,
    val parentHash: String,
    val blockTimestamp: ULong,
    val tronStakeData: TronStakeData,
) : ChainSignData

fun GemTransactionLoadMetadata.Tron.toChainData(): TronChainData {
    return TronChainData(
        blockNumber = blockNumber,
        blockVersion = blockVersion,
        blockTimestamp = blockTimestamp,
        txTrieRoot = transactionTreeRoot,
        witnessAddress = witnessAddress,
        parentHash = parentHash,
        tronStakeData = stakeData,
    )
}

fun TronChainData.toGem(): GemTransactionLoadMetadata.Tron = GemTransactionLoadMetadata.Tron(
    blockNumber = blockNumber,
    blockVersion = blockVersion,
    blockTimestamp = blockTimestamp,
    transactionTreeRoot = txTrieRoot,
    witnessAddress = witnessAddress,
    parentHash = parentHash,
    stakeData = tronStakeData,
)