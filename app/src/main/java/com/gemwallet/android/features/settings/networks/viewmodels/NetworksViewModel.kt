package com.gemwallet.android.features.settings.networks.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.stately.collections.ConcurrentMutableMap
import com.gemwallet.android.blockchain.clients.NodeStatusClientsProxy
import com.gemwallet.android.data.repositories.chains.ChainInfoRepository
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.config.NodesRepository
import com.gemwallet.android.ext.filter
import com.gemwallet.android.features.settings.networks.models.AddSourceType
import com.gemwallet.android.features.settings.networks.models.NetworksUIState
import com.gemwallet.android.model.NodeStatus
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Node
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableMap
import javax.inject.Inject
import kotlin.String

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NetworksViewModel @Inject constructor(
    private val chainInfoRepository: ChainInfoRepository,
    private val configRepository: ConfigRepository,
    private val nodesRepository: NodesRepository,
    private val nodeStatusClientsProxy: NodeStatusClientsProxy,
    private val nodeStatusClients: NodeStatusClientsProxy,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiState = state.map { it.toUIState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, NetworksUIState())
    val chainFilter = TextFieldState()
    val isRefreshing = MutableStateFlow<Long?>(null)

    var nodes: StateFlow<List<Node>> = state.flatMapLatest {
        if (it.chain == null) {
            return@flatMapLatest emptyFlow()
        }
        nodesRepository.getNodes(it.chain)
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val refreshingStatus = isRefreshing.filter { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    var nodeStates = nodes.combine(refreshingStatus) { nodes, isRefreshing -> Pair(nodes, isRefreshing) }
    .map { it.first }
    .flatMapLatest { nodes ->
        val chain = state.value.chain ?: return@flatMapLatest emptyFlow<Map<String, NodeStatus?>>()
        channelFlow {
            val statuses = ConcurrentMutableMap<String, NodeStatus?>()
            nodes.forEach { node ->
                statuses[node.url] = NodeStatus(
                    chainId = "",
                    blockNumber = "",
                    inSync = false,
                    latency = 0,
                    loading = true,
                )
            }
            send(statuses)

            withContext(Dispatchers.IO) {
                launch(Dispatchers.IO) {
                    delay(300)
                    isRefreshing.update { null }
                }
                nodes.map { node ->
                    async(Dispatchers.IO) {
                        val status = nodeStatusClients(chain, node.url)
                        statuses[node.url] = status
                        send(statuses.toImmutableMap())
                    }
                }.awaitAll()
                send(statuses.toImmutableMap())
            }

        }
    }
    .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    init {
        viewModelScope.launch {
            state.update { it.copy(availableChains = chainInfoRepository.getAll()) }
            snapshotFlow { chainFilter.text }.collectLatest { query ->
                state.update { it.copy(availableChains = chainInfoRepository.getAll().filter(query.toString().lowercase())) }
            }
        }
    }

    fun onSelectedChain(chain: Chain) {
        state.update {
            it.copy(
                chain = chain,
                selectChain = false,
                explorers = configRepository.getBlockExplorers(chain),
                currentNode = configRepository.getCurrentNode(chain),
                currentExplorer = configRepository.getCurrentBlockExplorer(chain),
                availableAddNode = nodeStatusClientsProxy.isMaintained(chain),
            )
        }
    }

    fun refresh() {
        isRefreshing.update { System.nanoTime() }
    }

    fun onSelectNode(node: Node) {
        val chain = state.value.chain ?: return
        configRepository.setCurrentNode(chain, node)
        onSelectedChain(chain)
    }

    fun onSelectBlockExplorer(name: String) {
        val chain = state.value.chain ?: return
        configRepository.setCurrentBlockExplorer(chain, name)
        onSelectedChain(chain)
    }

    fun onSelectChain() {
        state.update {it.copy(selectChain = true) }
    }

    private data class State(
        val chain: Chain? = null,
        val explorers: List<String> = emptyList(),
        val currentNode: Node? = null,
        val currentExplorer: String? = null,
        val availableChains: List<Chain> = emptyList(),
        val selectChain: Boolean = true,
        val availableAddNode: Boolean = true,
        val addSourceType: AddSourceType = AddSourceType.None,
    ) {
        fun toUIState(): NetworksUIState {
            return NetworksUIState(
                chain = chain,
                chains = availableChains,
                selectChain = selectChain,
                blockExplorers = explorers,
                currentNode = currentNode,
                currentExplorer = currentExplorer,
                availableAddNode = availableAddNode,
            )
        }
    }
}