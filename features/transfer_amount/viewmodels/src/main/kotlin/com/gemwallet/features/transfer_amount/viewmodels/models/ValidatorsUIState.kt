package com.gemwallet.features.transfer_amount.viewmodels.models

import com.wallet.core.primitives.DelegationValidator

sealed interface ValidatorsUIState {
    data object Loading : ValidatorsUIState

    data object Empty : ValidatorsUIState

    class Loaded(
        val recomended: List<DelegationValidator>,
        val validators: List<DelegationValidator>,
        val loading: Boolean = false,
    ) : ValidatorsUIState
}