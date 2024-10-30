package com.gemwallet.android.data.repositories.nodes

import com.gemwallet.android.data.database.NodesDao
import com.gemwallet.android.data.database.entities.DbNode
import com.gemwallet.android.data.repositories.config.ConfigRepository
import com.gemwallet.android.data.repositories.config.ConfigStore
import com.gemwallet.android.ext.findByString
import com.google.gson.Gson
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainNodes
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uniffi.Gemstone.Config
import kotlin.collections.groupBy
import kotlin.collections.map
import kotlin.text.ifEmpty

class NodesRepository(
    private val gson: Gson,
    private val nodesDao: NodesDao,
    private val configStore: ConfigStore,
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

    fun setCurrentNode(chain: Chain, node: Node) {
        configStore.putString(ConfigKey.CurrentNode.string, gson.toJson(node), chain.string)
    }

    fun getCurrentNode(chain: Chain): Node? {
        val data = configStore.getString(ConfigKey.CurrentNode.string, postfix = chain.string)
        val node = try {
            gson.fromJson(data, Node::class.java)
        } catch (_: Throwable) {
            return null
        }
        return node
    }

    fun getBlockExplorers(chain: Chain): List<String> {
        return Config().getBlockExplorers(chain.string)
    }

    fun getCurrentBlockExplorer(chain: Chain): String {
        return configStore.getString(ConfigKey.CurrentExplorer.string, chain.string).ifEmpty {
            getBlockExplorers(chain).firstOrNull() ?: ""
        }
    }

    fun setCurrentBlockExplorer(chain: Chain, name: String) {
        configStore.putString(ConfigKey.CurrentExplorer.string, name, chain.string)
    }

    private enum class ConfigKey(val string: String) {
        CurrentNode("current_node"),
        CurrentExplorer("current_explorer"),
        ;
    }
}