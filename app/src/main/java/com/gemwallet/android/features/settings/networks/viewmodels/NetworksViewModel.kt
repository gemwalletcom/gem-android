package com.gemwallet.android.features.settings.networks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.data.chains.ChainInfoRepository
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.features.settings.networks.models.AddSourceType
import com.gemwallet.android.features.settings.networks.models.NetworksUIState
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Node
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetworksViewModel @Inject constructor(
    private val chainInfoRepository: ChainInfoRepository,
    private val configRepository: ConfigRepository,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, NetworksUIState())

    init {
        viewModelScope.launch {
            state.update { it.copy(availableChains = chainInfoRepository.getAll()) }
        }
    }

    fun onSelectedChain(chain: Chain) {
        state.update {
            it.copy(
                chain = chain,
                selectChain = false,
                nodes = configRepository.getNode(chain),
                currentNode = configRepository.getCurrentNode(chain),
            )
        }
    }

    fun onSelectNode(node: Node) {
        val chain = state.value.chain ?: return
        configRepository.setCurrentNode(chain, node)
        onSelectedChain(chain)
    }

    fun onSelectChain() {
        state.update {it.copy(selectChain = true) }
    }

    fun onAddSource(type: AddSourceType) {
        state.update { it.copy(addSourceType = type) }
    }
    private data class State(
        val chain: Chain? = null,
        val nodes: List<Node> = emptyList(),
        val currentNode: Node? = null,
        val availableChains: List<Chain> = emptyList(),
        val selectChain: Boolean = true,
        val addSourceType: AddSourceType = AddSourceType.None,
    ) {
        fun toUIState(): NetworksUIState {
            return NetworksUIState(
                chain = chain,
                chains = availableChains,
                selectChain = selectChain,
                nodes = nodes,
                currentNode = currentNode,
                addSourceType = addSourceType,
            )
        }
    }
}