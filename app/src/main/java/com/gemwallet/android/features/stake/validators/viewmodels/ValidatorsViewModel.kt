package com.gemwallet.android.features.stake.validators.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.data.repositories.stake.StakeRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.stake.validators.model.ValidatorsError
import com.gemwallet.android.features.stake.validators.model.ValidatorsUIState
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.DelegationValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ValidatorsViewModel @Inject constructor(
    private val stakeRepository: StakeRepository,
    private val assetsRepository: AssetsRepository,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ValidatorsUIState.Loading)

    fun init(chain: Chain) {
        viewModelScope.launch {
            stakeRepository.getValidators(chain).collect { validators ->
                state.update {
                    it.copy(
                        loading = false,
                        validators = validators.filter { it.name.isNotEmpty() },
                        chain = chain,
                        recommended = stakeRepository.getRecommendValidators(chain)
                    )
                }
            }
        }
    }

    fun sync() = viewModelScope.launch {
        state.update { it.copy(loading = true) }
        val stakeApr = assetsRepository.getStakeApr(AssetId(state.value.chain!!)) ?: return@launch
        viewModelScope.launch {
            stakeRepository.syncValidators(state.value.chain, stakeApr)
        }
    }

    private data class State(
        val error: ValidatorsError? = null,
        val fatalError: ValidatorsError? = null,
        val loading: Boolean = false,
        val chain: Chain? = null,
        val recommended: List<String> = emptyList(),
        val validators: List<DelegationValidator> = emptyList(),
    ) {
        fun toUIState(): ValidatorsUIState {
            return when {
                chain == null -> ValidatorsUIState.Loading
                fatalError != null -> ValidatorsUIState.Fatal
                validators.isNotEmpty() -> ValidatorsUIState.Loaded(
                    loading = loading,
                    error = error,
                    chainTitle = chain.asset().name,
                    recomended = validators.filter { it.name.isNotEmpty() && recommended.contains(it.id) },
                    validators = validators,
                )
                else -> ValidatorsUIState.Empty
            }
        }
    }
}