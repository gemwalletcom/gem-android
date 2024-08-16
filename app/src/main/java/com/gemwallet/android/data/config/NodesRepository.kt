package com.gemwallet.android.data.config

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainNodes
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class NodesRepository(
    private val nodeLocalSource: NodeLocalSource,
) {

    suspend fun getNodes(chain: Chain): Flow<List<Node>> = withContext(Dispatchers.IO) {
        nodeLocalSource.getNodes().map { nodes: List<ChainNodes> ->
            listOf(ConfigRepository.getGemNode(chain)) +
                nodes.filter { it.chain == chain.string }.map { it.nodes }.flatten()
        }
    }

    suspend fun setNodes(nodes: List<ChainNodes>) = withContext(Dispatchers.IO) {
        nodeLocalSource.addNodes(nodes)
    }

    suspend fun addNode(chain: Chain, url: String) = withContext(Dispatchers.IO) {
        nodeLocalSource.addNodes(
            listOf(
                ChainNodes(
                    chain = chain.string,
                    nodes = listOf(
                        Node(
                            url = url,
                            status = NodeState.Active,
                            priority = 0,
                        )
                    )
                )
            )
        )
    }
}