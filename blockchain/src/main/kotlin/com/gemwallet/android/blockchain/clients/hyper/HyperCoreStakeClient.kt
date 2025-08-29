package com.gemwallet.android.blockchain.clients.hyper

import com.gemwallet.android.blockchain.clients.StakeClient
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationValidator

class HyperCoreStakeClient(
    private val chain: Chain
) : StakeClient {
    override suspend fun getValidators(
        chain: Chain,
        apr: Double
    ): List<DelegationValidator> {
        TODO("Not yet implemented")
    }

    override suspend fun getStakeDelegations(
        chain: Chain,
        address: String,
        apr: Double
    ): List<DelegationBase> {
        TODO("Not yet implemented")
    }

    override fun supported(chain: Chain): Boolean = this.chain == chain
}