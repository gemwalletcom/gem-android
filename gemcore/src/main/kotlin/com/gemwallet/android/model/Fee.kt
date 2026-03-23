package com.gemwallet.android.model

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.FeePriority
import java.math.BigInteger

sealed interface Fee {

    val priority: FeePriority
    val feeAssetId: AssetId

    val amount: BigInteger

    class Plain(
        override val feeAssetId: AssetId,
        override val priority: FeePriority,
        override val amount: BigInteger,
        val options: Map<String, BigInteger>,
    ) : Fee

    class Regular(
        override val feeAssetId: AssetId,
        override val priority: FeePriority,
        override val amount: BigInteger,
        val maxGasPrice: BigInteger,
        val limit: BigInteger,
        val options: Map<String, BigInteger>,
    ) : Fee

    class Eip1559(
        override val feeAssetId: AssetId,
        override val priority: FeePriority,
        override val amount: BigInteger,
        val maxGasPrice: BigInteger,
        val minerFee: BigInteger,
        val limit: BigInteger,
        val options: Map<String, BigInteger>,
    ) : Fee

    class Solana(
        override val feeAssetId: AssetId,
        override val priority: FeePriority,
        override val amount: BigInteger,
        val minerFee: BigInteger,
        val maxGasPrice: BigInteger,
        val unitFee: BigInteger,
        val limit: BigInteger,
        val options: Map<String, BigInteger>,
    ) : Fee
}