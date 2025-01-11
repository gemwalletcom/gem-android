package com.gemwallet.android.blockchain.clients.polkadot

import com.gemwallet.android.model.Fee
import com.gemwallet.android.model.TxSpeed
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import java.math.BigInteger

class PolkadotFeeCalculator {
    fun calculate(): Fee = Fee(speed = TxSpeed.Normal, feeAssetId = AssetId(Chain.Polkadot), amount = BigInteger.ONE)
}