package com.gemwallet.features.earn.delegation.models

import com.wallet.core.primitives.DelegationState
import com.wallet.core.primitives.DelegationValidator

sealed interface DelegationProperty {
    class Name(val data: String) : DelegationProperty

    class Apr(val data: DelegationValidator) : DelegationProperty

    class State(val state: DelegationState, val availableIn: String) : DelegationProperty

    class TransactionStatus(val state: DelegationState, val isActive: Boolean) : DelegationProperty
}

sealed interface DelegationBalances {
    class Stake(val data: String) : DelegationBalances

    class Rewards(val data: String) : DelegationBalances
}

sealed interface DelegationActions {
    object WithdrawalAction : DelegationActions

    object StakeAction : DelegationActions

    object UnstakeAction : DelegationActions

    object RedelegateAction : DelegationActions
}