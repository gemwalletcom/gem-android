package com.gemwallet.android.blockchain.clients.polkadot.models

import java.math.BigInteger

data class PolkadotSigningData(
    val genesisHash: String,
    val blockHash: String,
    val blockNumber: BigInteger,
    val specVersion: BigInteger,
    val transactionVersion: BigInteger,
    val period: Long,
)
