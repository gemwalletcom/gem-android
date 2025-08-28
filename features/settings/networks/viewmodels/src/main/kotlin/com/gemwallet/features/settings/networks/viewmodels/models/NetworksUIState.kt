package com.gemwallet.features.settings.networks.viewmodels.models

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Node

class NetworksUIState(
    val selectChain: Boolean = true,
    val chain: Chain? = null,
    val chains: List<Chain> = emptyList(),
    val nodes: List<Node> = emptyList(),
    val blockExplorers: List<String> = emptyList(),
    val currentNode: Node? = null,
    val currentExplorer: String? = null,
    val availableAddNode: Boolean = false,
) {
}