package com.gemwallet.android.blockchain.clients.polkadot.models

import com.gemwallet.android.serializer.BigIntegerSerializer
import kotlinx.serialization.Serializable
import java.math.BigInteger

@Serializable
data class PolkadotSigningData(
    val genesisHash: String,
    val blockHash: String,
    @Serializable(BigIntegerSerializer::class) val blockNumber: BigInteger,
    @Serializable(BigIntegerSerializer::class) val specVersion: BigInteger,
    @Serializable(BigIntegerSerializer::class) val transactionVersion: BigInteger,
    val period: Long,
)
