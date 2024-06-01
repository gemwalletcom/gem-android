package com.gemwallet.android.model

import com.wallet.core.primitives.AssetId
import java.math.BigInteger

open class Fee(val feeAssetId: AssetId, val amount: BigInteger, val options: Map<String, BigInteger> = emptyMap()) {
    fun withOptions(key: String) = Fee(
        feeAssetId = feeAssetId,
        amount = amount + options
            .filterKeys { it.contains(key) }
            .values.fold(BigInteger.ZERO) { acc, i -> acc + i }, // TODO: Change design for it
        options = options
    )
}

class GasFee(
    feeAssetId: AssetId,
    val maxGasPrice: BigInteger,
    val limit: BigInteger,
    val minerFee: BigInteger = BigInteger.ZERO,
    val relay: BigInteger = BigInteger.ZERO,
    amount: BigInteger = limit.multiply(maxGasPrice).add(relay),
) : Fee(feeAssetId = feeAssetId, amount = amount)
