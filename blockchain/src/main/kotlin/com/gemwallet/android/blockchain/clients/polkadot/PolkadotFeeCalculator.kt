package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.model.Fee
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.FeePriority
import java.math.BigInteger

class PolkadotFeeCalculator {
    fun calculate(): Fee = Fee(priority = FeePriority.Normal, feeAssetId = AssetId(Chain.Polkadot), amount = BigInteger.ONE)
}