package com.gemwallet.android.interactors.sync

import com.gemwallet.android.data.config.NodesRepository
import com.gemwallet.android.interactors.SyncOperator
import com.wallet.core.primitives.ChainNodes
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeStatus
import uniffi.Gemstone.NodePriority

class SyncNodes(
    private val nodesRepository: NodesRepository,
) : SyncOperator {

    override suspend fun invoke() {
        val nodes = uniffi.Gemstone.Config().getNodes().map { entry ->
            ChainNodes(
                chain = entry.key,
                nodes = entry.value.map {
                    Node(
                        url = it.url,
                        status = when (it.priority) {
                            NodePriority.HIGH,
                            NodePriority.MEDIUM,
                            NodePriority.LOW -> NodeStatus.Active
                            NodePriority.INACTIVE -> NodeStatus.Inactive
                        },
                        priority = when (it.priority) {
                            NodePriority.HIGH -> 3
                            NodePriority.MEDIUM -> 2
                            NodePriority.LOW -> 1
                            NodePriority.INACTIVE -> 0
                        }
                    )
                }
            )
        }
        nodesRepository.setNodes(nodes)
    }
}