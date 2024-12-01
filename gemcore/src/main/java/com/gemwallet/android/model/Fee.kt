package com.gemwallet.android.model

import com.wallet.core.primitives.AssetId
import java.math.BigInteger

open class Fee(val speed: TxSpeed, val feeAssetId: AssetId, val amount: BigInteger, val options: Map<String, BigInteger> = emptyMap()) {
    open fun withOptions(key: String) = Fee(
        feeAssetId = feeAssetId,
        speed = speed,
        amount = amount + options
            .filterKeys { it.contains(key) }
            .values.fold(BigInteger.ZERO) { acc, i -> acc + i },
        options = options,
    )
}

class GasFee(
    feeAssetId: AssetId,
    speed: TxSpeed,
    val maxGasPrice: BigInteger,
    val limit: BigInteger,
    val minerFee: BigInteger = BigInteger.ZERO,
    val relay: BigInteger = BigInteger.ZERO,
    amount: BigInteger = limit.multiply(maxGasPrice).add(relay),
    options: Map<String, BigInteger> = emptyMap()
) : Fee(feeAssetId = feeAssetId, speed = speed, amount = amount, options = options) {
    override fun withOptions(key: String) = GasFee(
        feeAssetId = feeAssetId,
        speed = speed,
        maxGasPrice = maxGasPrice,
        limit = limit,
        minerFee = minerFee,
        relay = relay,
        amount = amount + options
            .filterKeys { it.contains(key) }
            .values.fold(BigInteger.ZERO) { acc, i -> acc + i },
        options = options,
    )
}
