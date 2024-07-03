package com.gemwallet.android.features.import_wallet.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.chains.ChainInfoRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.filter
import com.gemwallet.android.features.assets.model.IconUrl
import com.gemwallet.android.interactors.getIconUrl
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.WalletType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectImportTypeViewModel @Inject constructor(
    private val chainInfoRepository: ChainInfoRepository,
) : ViewModel() {
    private val state = MutableStateFlow(SelectChainViewModelState())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SelectImportTypeUIState())
    val chainFilter = TextFieldState()

    init {
        viewModelScope.launch {
            snapshotFlow{ chainFilter.text }.collectLatest { query ->
                state.update { old -> old.copy(
                    chains = chainInfoRepository.getAll().filter(query.toString().lowercase())
                ) }
            }
        }
        viewModelScope.launch {
            state.update { it.copy(chains = chainInfoRepository.getAll()) }
        }
    }

}

data class SelectChainViewModelState(
    val chains: List<Chain> = emptyList(),
) {
    fun toUIState() = SelectImportTypeUIState(
        chains = chains.map {
            ChainUIState(
                chain = it,
                title = when (it) {
                    Chain.Tron -> it.asset().name.toUpperCase(Locale.current)
                    else -> it.asset().name
                },
                icon = it.getIconUrl()
            )
        }
    )
}

data class SelectImportTypeUIState(
    val chains: List<ChainUIState> = emptyList()
)

data class ChainUIState(
    val chain: Chain,
    val title: String,
    val icon: IconUrl,
)

data class ImportType(
    val walletType: WalletType,
    val chain: Chain? = null,
)