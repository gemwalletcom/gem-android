package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationValidator

interface StakeClient : BlockchainClient {
    suspend fun getValidators(chain: Chain, apr: Double): List<DelegationValidator>

    suspend fun getStakeDelegations(chain: Chain, address: String, apr: Double): List<DelegationBase>
}