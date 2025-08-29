package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.BalanceClient
import com.gemwallet.android.model.AssetBalance
import com.wallet.core.primitives.Chain

class HyperCoreBalanceClient(
    private val chain: Chain,
) : BalanceClient {
    override suspend fun getNativeBalance(
        chain: Chain,
        address: String
    ): AssetBalance? {
        TODO("Not yet implemented")
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}