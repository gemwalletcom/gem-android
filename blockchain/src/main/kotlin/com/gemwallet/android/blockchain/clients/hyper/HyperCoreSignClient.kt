package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.SignClient
import com.wallet.core.primitives.Chain

class HyperCoreSignClient(
    val chain: Chain,
) : SignClient {
    override fun supported(chain: Chain): Boolean = this.chain == chain
}