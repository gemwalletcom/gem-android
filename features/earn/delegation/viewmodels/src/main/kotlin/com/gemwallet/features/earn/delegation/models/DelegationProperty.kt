package com.gemwallet.features.earn.delegation.models

import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator

sealed interface DelegationProperty {
    class Name(val data: String) : DelegationProperty

    class Apr(val data: DelegationValidator) : DelegationProperty

    class State(val state: DelegationState, val availableIn: String) : DelegationProperty

    class TransactionStatus(val state: DelegationState, val isActive: Boolean) : DelegationProperty
}