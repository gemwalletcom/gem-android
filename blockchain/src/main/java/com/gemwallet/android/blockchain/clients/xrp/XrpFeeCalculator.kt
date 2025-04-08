package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority

class XrpFeeCalculator(
    private val chain: Chain,
    private val rpcClient: XrpRpcClient,
) {
    suspend fun calculate(): Fee {
        val median = rpcClient.fee().getOrThrow().result.drops.median_fee.toBigInteger()
        return Fee(feeAssetId = AssetId(chain), priority = FeePriority.Normal, amount = median)
    }
}