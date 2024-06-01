package com.gemwallet.android.features.stake.validators.model

import com.wallet.core.primitives.DelegationValidator

sealed interface ValidatorsUIState {
    data object Loading : ValidatorsUIState

    data object Fatal : ValidatorsUIState

    data object Empty : ValidatorsUIState

    class Loaded(
        val chainTitle: String,
        val recomended: List<DelegationValidator>,
        val validators: List<DelegationValidator>,
        val loading: Boolean = false,
        val error: ValidatorsError? = null,
    ) : ValidatorsUIState
}