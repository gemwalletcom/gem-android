package com.gemwallet.android.data.repositoreis.nodes

import com.gemwallet.android.cases.nodes.AddNodeCase
import com.gemwallet.android.cases.nodes.GetBlockExplorers
import com.gemwallet.android.cases.nodes.GetCurrentBlockExplorer
import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.cases.nodes.GetNodesCase
import com.gemwallet.android.cases.nodes.SetBlockExplorerCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.gemwallet.android.cases.nodes.getGemNode
import com.gemwallet.android.data.service.store.ConfigStore
import com.gemwallet.android.data.service.store.database.NodesDao
import com.gemwallet.android.data.service.store.database.entities.DbNode
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.findByString
import com.gemwallet.android.ext.getSwapMetadata
import com.gemwallet.android.ext.hash
import com.gemwallet.android.model.Transaction
import com.gemwallet.android.serializer.jsonEncoder
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChainNodes
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState
import com.wallet.core.primitives.SwapProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.gemstone.Config
import uniffi.gemstone.Explorer
import uniffi.gemstone.NodePriority

class NodesRepository(
    private val nodesDao: NodesDao,
    private val configStore: ConfigStore,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : SetCurrentNodeCase,
    GetCurrentNodeCase,
    SetBlockExplorerCase,
    GetBlockExplorers,
    GetCurrentBlockExplorer,
    GetNodesCase,
    AddNodeCase
{

    init {
        scope.launch { sync() }
    }

    override suspend fun getNodes(chain: Chain): Flow<List<Node>> = withContext(Dispatchers.IO) {
        nodesDao.getNodes().map { nodes ->
            nodes.groupBy { it.chain }.map { entry ->
                ChainNodes(
                    chain = entry.key.string,
                    nodes = entry.value.map { Node(it.url, it.status, it.priority) }
                )
            }
        }
        .map { nodes -> nodes.filter { it.chain == chain.string }.map { it.nodes }.flatten() }
        .map { nodes -> listOf(getGemNode(chain)) + nodes }
    }

    override suspend fun addNode(chain: Chain, url: String) = withContext(Dispatchers.IO) {
        nodesDao.addNodes(listOf(DbNode(url, NodeState.Active, 0, chain)))
    }

    override fun setCurrentNode(chain: Chain, node: Node) {
        configStore.putString(
            ConfigKey.UsageNode.string,
            jsonEncoder.encodeToString(node),
            chain.string
        )
    }

    override fun getCurrentNode(chain: Chain): Node? {
        val data = configStore.getString(
            ConfigKey.UsageNode.string,
            postfix = chain.string
        )
        val node = try {
            jsonEncoder.decodeFromString<Node>(data)
        } catch (_: Throwable) {
            return null
        }
        return node
    }

    override fun getBlockExplorers(chain: Chain): List<String> {
        return Config().getBlockExplorers(chain.string)
    }

    override fun getCurrentBlockExplorer(chain: Chain): String {
        return configStore.getString(
            ConfigKey.CurrentExplorer.string,
            chain.string
        ).ifEmpty {
            getBlockExplorers(chain).firstOrNull() ?: ""
        }
    }

    override fun getBlockExplorerInfo(transaction: Transaction): Pair<String, String> {
        val chain = transaction.assetId.chain
        val swapMetadata = transaction.getSwapMetadata()
        val provider = swapMetadata?.provider
        val swapProvider = SwapProvider.entries.firstOrNull { it.string == provider }

        val identifier = when (swapProvider) {
            SwapProvider.NearIntents -> transaction.to
            else -> transaction.hash
        }

        val blockExplorerName = getCurrentBlockExplorer(chain)
        val explorer = Explorer(chain.string)
        val swapExplorerUrl = provider?.let { explorer.getTransactionSwapUrl(blockExplorerName, identifier, provider) }
        val explorerUrl = swapExplorerUrl?.url ?: explorer.getTransactionUrl(blockExplorerName, transaction.hash)
        return Pair(
            explorerUrl,
            swapExplorerUrl?.name ?: blockExplorerName,
        )
    }

    override fun setCurrentBlockExplorer(chain: Chain, name: String) {
        configStore.putString(
            ConfigKey.CurrentExplorer.string,
            name,
            chain.string
        )
    }

    private suspend fun sync() {
        val nodes = Config().getNodes().mapNotNull { entry ->
            entry.value.mapNotNull {
                DbNode(
                    chain = Chain.findByString(entry.key) ?: return@mapNotNull null,
                    url = it.url,
                    status = when (it.priority) {
                        NodePriority.HIGH,
                        NodePriority.MEDIUM,
                        NodePriority.LOW -> NodeState.Active
                        NodePriority.INACTIVE -> NodeState.Inactive
                    },
                    priority = when (it.priority) {
                        NodePriority.HIGH -> 3
                        NodePriority.MEDIUM -> 2
                        NodePriority.LOW -> 1
                        NodePriority.INACTIVE -> 0
                    }
                )
            }
        }.flatten()
        nodesDao.addNodes(nodes)
    }

    private enum class ConfigKey(val string: String) {
        UsageNode("usage_node"),
        CurrentExplorer("current_explorer"),
        ;
    }
}