package com.gemwallet.android.blockchain.clients.xrp

import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain

class XrpFee {
    suspend operator fun invoke(
        chain: Chain,
        rpcClient: XrpRpcClient,
    ): Fee {
        val median = rpcClient.fee().getOrThrow().result.drops.median_fee.toBigInteger()
        return Fee(feeAssetId = AssetId(chain), speed = TxSpeed.Normal, amount = median)
    }
}