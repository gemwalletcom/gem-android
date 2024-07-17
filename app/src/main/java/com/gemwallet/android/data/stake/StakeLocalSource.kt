package com.gemwallet.android.data.stake

import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationBase
import com.wallet.core.primitives.DelegationValidator
import kotlinx.coroutines.flow.Flow

interface StakeLocalSource {
    suspend fun update(validators: List<DelegationValidator>)

    suspend fun update(address: String, delegations: List<DelegationBase>)

    suspend fun getValidators(chain: Chain): Flow<List<DelegationValidator>>

    suspend fun getDelegations(assetId: AssetId, address: String): Flow<List<Delegation>>

    suspend fun getDelegation(validatorId: String, delegationId: String): Flow<Delegation?>

    suspend fun getStakeValidator(assetId: AssetId): DelegationValidator?

    suspend fun getStakeValidator(assetId: AssetId, validatorId: String): DelegationValidator?

    suspend fun getUnstakeValidator(assetId: AssetId, address: String): DelegationValidator?
}