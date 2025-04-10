package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import java.math.BigInteger

class XrpFeeCalculator(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) {
    suspend fun calculate(): List<Fee> {
        val drops = rpcClient.fee().getOrThrow().result.drops
        val minimum = drops.minimum_fee.toBigInteger()
        val median = drops.median_fee.toBigInteger()

        return FeePriority.entries.map {
            val amount = when (it) {
                FeePriority.Slow -> (median / BigInteger.TEN).max(minimum)
                FeePriority.Normal -> median
                FeePriority.Fast -> median * BigInteger.valueOf(2)
            }
            Fee(feeAssetId = AssetId(chain), priority = it, amount = amount)
        }
    }
}