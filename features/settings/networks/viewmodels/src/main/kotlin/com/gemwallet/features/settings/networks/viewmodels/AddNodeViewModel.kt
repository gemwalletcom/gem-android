package com.gemwallet.features.settings.networks.viewmodels

import android.util.Patterns
import android.webkit.URLUtil
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemwallet.android.blockchain.clients.NodeStatusClientProxy
import com.gemwallet.android.cases.nodes.AddNodeCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.gemwallet.android.model.NodeStatus
import com.gemwallet.features.settings.networks.viewmodels.models.AddNodeUIModel
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNodeViewModel @Inject constructor(
    private val nodeStatusClient: NodeStatusClientProxy,
    private val addNodeCase: AddNodeCase,
    private val setCurrentNodeCase: SetCurrentNodeCase,
) : ViewModel() {

    private val state = MutableStateFlow(State())
    val uiModel = state.map { it.toUIModel() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AddNodeUIModel())
    val url = mutableStateOf("")

    fun init(chain: Chain) {
        state.update { State(chain = chain) }
    }

    private fun checkUrl(url: String) {
        viewModelScope.launch {
            state.update { it.copy(checking = true, nodeState = null) }
            val chain = state.value.chain ?: return@launch
            val status = nodeStatusClient.getNodeStatus(chain, url)
            state.update { it.copy(nodeState = status, checking = false) }
        }
    }

    fun addUrl() {
        val chain = state.value.chain ?: return
        viewModelScope.launch {
            addNodeCase.addNode(chain = chain, url.value)
            setCurrentNodeCase.setCurrentNode(chain = chain, Node(url.value, status = NodeState.Active, 0))
            url.value = ""
        }
    }

    fun onUrlChange() {
        state.update { it.copy(nodeState = null) }

        if (URLUtil.isNetworkUrl(url.value) && Patterns.WEB_URL.matcher(url.value).matches()) {
            checkUrl(url.value)
        }
    }

    private data class State(
        val chain: Chain? = null,
        val nodeState: NodeStatus? = null,
        val checking: Boolean = false,
    ) {
        fun toUIModel(): AddNodeUIModel {
            return AddNodeUIModel(
                chain = chain,
                status = nodeState,
                checking = checking
            )
        }
    }
}