package com.gemwallet.android.data.repositories.nodes

import com.gemwallet.android.data.repositories.config.ConfigRepository
import com.gemwallet.android.data.database.NodesDao
import com.gemwallet.android.data.database.entities.DbNode
import com.gemwallet.android.ext.findByString
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainNodes
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.collections.groupBy
import kotlin.collections.map

class NodesRepository(
    private val nodesDao: NodesDao,
) {

    suspend fun getNodes(chain: Chain): Flow<List<Node>> = withContext(Dispatchers.IO) {
        nodesDao.getNodes().map { nodes ->
            nodes.groupBy { it.chain }.map { entry ->
                ChainNodes(
                    chain = entry.key.string,
                    nodes = entry.value.map {
                        Node(
                            url = it.url,
                            priority = it.priority,
                            status = it.status,
                        )
                    }
                )
            }
        }
        .map { nodes: List<ChainNodes> ->
            listOf(ConfigRepository.Companion.getGemNode(chain)) +
                    nodes.filter { it.chain == chain.string }.map { it.nodes }.flatten()
        }
    }

    suspend fun setNodes(nodes: List<ChainNodes>) = withContext(Dispatchers.IO) {
        nodesDao.addNodes(
            nodes.map { node ->
                node.nodes.mapNotNull {
                    DbNode(
                        chain = Chain.findByString(node.chain) ?: return@mapNotNull null,
                        url = it.url,
                        status = it.status,
                        priority = it.priority,
                    )
                }
            }.flatten()
        )
    }

    suspend fun addNode(chain: Chain, url: String) = withContext(Dispatchers.IO) {
        val nodes = listOf(
            DbNode(
                chain = chain,
                url = url,
                status = NodeState.Active,
                priority = 0,
            )
        )
        nodesDao.addNodes(nodes)
    }
}