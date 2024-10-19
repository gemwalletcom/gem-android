@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.gemwallet.android.features.settings.networks.views

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.settings.networks.models.NetworksUIState
import com.gemwallet.android.model.NodeStatus
import com.gemwallet.android.ui.components.CircularProgressIndicator16
import com.gemwallet.android.ui.components.ListItem
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SubheaderItem
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.Spacer2
import com.gemwallet.android.ui.theme.Spacer4
import com.gemwallet.android.ui.theme.padding8
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Node

@OptIn(
    ExperimentalGetImage::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun NetworkScene(
    state: NetworksUIState,
    nodes: List<Node>,
    nodeStates: Map<String, NodeStatus?>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onSelectNode: (Node) -> Unit,
    onSelectBlockExplorer: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val chain = state.chain ?: return
    var isShowAddSource by remember { mutableStateOf(false) }

    Scene(
        title = chain.asset().name,
        actions = {
            if (state.availableAddNode) {
                IconButton(onClick = { isShowAddSource = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "")
                }
            }
        },
        onClose = onCancel,
    ) {
        val pullToRefreshState = rememberPullToRefreshState()
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        ) {
            LazyColumn {
                item {
                    SubheaderItem(
                        title = stringResource(id = R.string.settings_networks_source),
                    )
                }
                items(nodes) { node: Node ->
                    NodeItem(
                        state.chain,
                        state.currentNode,
                        node,
                        nodeStates[node.url],
                        onSelectNode
                    )
                }
                item {
                    Spacer16()
                    SubheaderItem(
                        title = stringResource(id = R.string.settings_networks_explorer),
                    )
                }
                items(state.blockExplorers) {
                    BlockExplorerItem(state.currentExplorer, it, onSelectBlockExplorer)
                }
            }
        }
    }
    AnimatedVisibility(visible = isShowAddSource,
        label = "",
        enter = slideIn { IntOffset(it.width, 0) },
        exit = slideOut { IntOffset(it.width, 0) },
    ) {
        AddNodeScene(
            chain = chain,
            onCancel = {
                isShowAddSource = false
                onRefresh()
            },
        )
    }
}

@Composable
private fun NodeItem(
    chain: Chain,
    current: Node?,
    node: Node,
    nodeStatus: NodeStatus?,
    onSelect: (Node) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onSelect(node) },
        dividerShowed = true,
        leading = if (node.url == current?.url) {
            @Composable {

                Icon(
                    modifier = Modifier
                        .padding(end = padding8)
                        .size(20.dp),
                    imageVector = Icons.Default.Done,
                    contentDescription = ""
                )
            }
        } else null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = if (node.url == ConfigRepository.getGemNodeUrl(chain)) {
                        "Gem Wallet Node"
                    } else {
                        node.url.replace("https://", "").replace("http://", "")
                    },
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer2()
                Text(
                    text = "${stringResource(R.string.nodes_import_node_latest_block)} - ${nodeStatus?.blockNumber ?: ""}",
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (nodeStatus?.loading == true) {
                    CircularProgressIndicator16()
                    return@Row
                }
                nodeStatus?.latency?.let {
                    Text(
                        text = stringResource(R.string.common_latency_in_ms, it)
                    )
                }
                Spacer4()
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            if (nodeStatus?.inSync == true) {
                                when {
                                    nodeStatus.latency < 1024 -> MaterialTheme.colorScheme.tertiary
                                    nodeStatus.latency < 2048 -> Color(0xffff9314)
                                    else -> MaterialTheme.colorScheme.error
                                }

                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            shape = CircleShape
                        )
                    ,
                ) {}
            }
        }
    }
}

@Composable
private fun BlockExplorerItem(
    current: String?,
    explorerName: String,
    onSelect: (String) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onSelect(explorerName) },
        dividerShowed = true,
        trailing = {
            if (explorerName == current) {
                Icon(
                    modifier = Modifier
                        .padding(end = padding8)
                        .size(20.dp),
                    imageVector = Icons.Default.Done,
                    contentDescription = ""
                )
            }
        }
    ) {
        Text(
            text = explorerName,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}