package com.gemwallet.android.blockchain.clients

import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationValidator

interface StakeClient : BlockchainClient {
    suspend fun getValidators(apr: Double): List<DelegationValidator>

    suspend fun getStakeDelegations(address: String, apr: Double): List<DelegationBase>
}