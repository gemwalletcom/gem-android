package com.gemwallet.android.domains.stake

import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Delegation
import com.wallet.core.primitives.DelegationState
import uniffi.gemstone.GemDelegation
import uniffi.gemstone.GemDelegationBase
import uniffi.gemstone.GemDelegationState

fun Delegation.toGem(chain: uniffi.gemstone.Chain): GemDelegation {
    return GemDelegation(
        base = GemDelegationBase(
            assetId = base.assetId.toIdentifier(),
            delegationId = base.delegationId,
            validatorId = base.validatorId,
            balance = base.balance,
            shares = base.shares,
            completionDate = base.completionDate,
            state = when (base.state) {
                DelegationState.Active -> GemDelegationState.ACTIVE
                DelegationState.Pending -> GemDelegationState.PENDING
                DelegationState.Undelegating -> GemDelegationState.UNDELEGATING
                DelegationState.Inactive -> GemDelegationState.INACTIVE
                DelegationState.Activating -> GemDelegationState.ACTIVATING
                DelegationState.Deactivating -> GemDelegationState.DEACTIVATING
                DelegationState.AwaitingWithdrawal -> GemDelegationState.AWAITING_WITHDRAWAL
            },
            rewards = base.rewards,
        ),
        validator = validator.toGem(chain),
        price = null,
    )
}