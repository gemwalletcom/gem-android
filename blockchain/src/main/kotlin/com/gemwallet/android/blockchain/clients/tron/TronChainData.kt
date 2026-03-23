package com.gemwallet.android.blockchain.clients.tron

import com.gemwallet.android.model.ChainSignData
import uniffi.gemstone.GemTransactionLoadMetadata
import uniffi.gemstone.TronStakeData

data class TronChainData(
    val blockNumber: ULong,
    val blockVersion: ULong,
    val transactionTreeRoot: String,
    val witnessAddress: String,
    val parentHash: String,
    val blockTimestamp: ULong,
    val stakeData: TronStakeData,
) : ChainSignData {
    override fun toDto(): GemTransactionLoadMetadata {
        return GemTransactionLoadMetadata.Tron(
            blockNumber = blockNumber,
            blockVersion = blockVersion,
            blockTimestamp = blockTimestamp,
            transactionTreeRoot = transactionTreeRoot,
            witnessAddress = witnessAddress,
            parentHash = parentHash,
            stakeData = stakeData,
        )
    }
}

fun GemTransactionLoadMetadata.Tron.toChainData(): TronChainData {
    return TronChainData(
        blockNumber = blockNumber,
        blockVersion = blockVersion,
        blockTimestamp = blockTimestamp,
        transactionTreeRoot = transactionTreeRoot,
        witnessAddress = witnessAddress,
        parentHash = parentHash,
        stakeData = stakeData,
    )
}