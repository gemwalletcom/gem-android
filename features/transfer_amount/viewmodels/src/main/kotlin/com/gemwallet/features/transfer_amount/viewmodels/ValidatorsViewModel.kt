package com.gemwallet.features.transfer_amount.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.repositoreis.stake.StakeRepository
import com.gemwallet.features.transfer_amount.viewmodels.models.ValidatorsUIState
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ValidatorsViewModel @Inject constructor(
    private val stakeRepository: StakeRepository,
    val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assetId = MutableStateFlow<AssetId?>(null)
    val validators = assetId.filterNotNull()
        .flatMapLatest { stakeRepository.getValidators(it.chain) }
        .map { it.filter { it.name.isNotEmpty() } }
        .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, emptyList())

    val uiState = combine(assetId, validators) { assetId, validators ->
        when {
            assetId == null -> ValidatorsUIState.Loading
            validators.isNotEmpty() -> {
                val recommended = stakeRepository.getRecommendValidators(assetId.chain)
                ValidatorsUIState.Loaded(
                    loading = false,
                    recomended = validators.filter { it.name.isNotEmpty() && recommended.contains(it.id) },
                    validators = validators,
                )
            }

            else -> ValidatorsUIState.Empty
        }
    }.stateIn(viewModelScope, SharingStarted.Companion.Eagerly, ValidatorsUIState.Loading)

    fun init(chain: Chain) {
        assetId.update { AssetId(chain) }
    }
}