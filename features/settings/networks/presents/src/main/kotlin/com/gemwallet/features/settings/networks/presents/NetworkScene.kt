@file:OptIn(ExperimentalMaterial3Api::class)
package com.gemwallet.features.settings.networks.presents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ext.asset
import com.gemwallet.android.model.NodeStatus
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.features.settings.networks.viewmodels.models.NetworksUIState
import com.wallet.core.primitives.Node

@Composable
fun NetworkScene(
    state: NetworksUIState,
    nodes: List<Node>,
    nodeStates: List<NodeStatus?>,
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
                val size = nodes.size
                itemsIndexed(nodes) { index, node: Node ->
                    NodeItem(
                        chain = state.chain!!,
                        node = node,
                        listPosition = ListPosition.getPosition(index, size),
                        selected = state.currentNode?.url == node.url,
                        nodeStatus = nodeStates.firstOrNull { it?.url == node.url },
                        onSelect = onSelectNode,
                    )
                }
                item {
                    SubheaderItem(
                        title = stringResource(id = R.string.settings_networks_explorer),
                    )
                }
                itemsPositioned(state.blockExplorers) { position, item ->
                    BlockExplorerItem(state.currentExplorer, item, position, onSelectBlockExplorer)
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
private fun BlockExplorerItem(
    current: String?,
    explorerName: String,
    listPosition: ListPosition,
    onSelect: (String) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable { onSelect(explorerName) },
        title = {
            ListItemTitleText(text = explorerName)
        },
        listPosition = listPosition,
        trailing = if (explorerName == current) {
            @Composable {
                Icon(
                    modifier = Modifier.Companion.padding(end = paddingSmall).size(20.dp),
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else null
    )
}