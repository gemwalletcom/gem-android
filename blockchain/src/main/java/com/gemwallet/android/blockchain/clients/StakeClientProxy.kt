package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationValidator

class StakeClientProxy(
    private val clients: List<StakeClient>
) : StakeClient {
    override suspend fun getValidators(
        chain: Chain,
        apr: Double
    ): List<DelegationValidator> {
        return clients.getClient(chain)?.getValidators(chain, apr) ?: emptyList()
    }

    override suspend fun getStakeDelegations(
        chain: Chain,
        address: String,
        apr: Double
    ): List<DelegationBase> {
        return clients.getClient(chain)?.getStakeDelegations(chain, address, apr) ?: emptyList()
    }

    override fun isMaintain(chain: Chain): Boolean {
        return clients.getClient(chain) != null
    }
}