package com.gemwallet.android.domains.stake

import com.wallet.core.primitives.DelegationValidator
import uniffi.gemstone.GemDelegationValidator

fun DelegationValidator.toGem(chain: uniffi.gemstone.Chain): GemDelegationValidator {
    return GemDelegationValidator(
        chain = chain,
        id = id,
        name = name,
        isActive = isActive,
        commission = commission,
        apr = apr,
    )
}