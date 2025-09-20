package com.gemwallet.android.domains.stake

import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.ConfirmParams
import com.wallet.core.primitives.Delegation
import uniffi.gemstone.GemDelegation
import uniffi.gemstone.GemDelegationBase

fun Delegation.toGem(chain: uniffi.gemstone.Chain): GemDelegation {
    return GemDelegation(
        base = GemDelegationBase(
            assetId = base.assetId.toIdentifier(),
            delegationId = base.delegationId,
            validatorId = base.validatorId,
            balance = base.balance,
            shares = base.shares,
            completionDate = base.completionDate?.toULong(),
            delegationState = base.state.string,
            rewards = base.rewards,
        ),
        validator = validator.toGem(chain)
    )
}