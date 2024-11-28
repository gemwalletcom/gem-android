package com.gemwallet.android.features.settings.networks.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.cases.nodes.getGemNodeUrl
import com.gemwallet.android.model.NodeStatus
import com.gemwallet.android.ui.components.ListItem
import com.gemwallet.android.ui.components.designsystem.Spacer6
import com.gemwallet.android.ui.components.designsystem.padding4
import com.gemwallet.android.ui.components.designsystem.padding8
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator14
import com.gemwallet.android.ui.theme.WalletTheme
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Node
import com.wallet.core.primitives.NodeState

@Composable
internal fun NodeItem(
    chain: Chain,
    node: Node,
    selected: Boolean,
    nodeStatus: NodeStatus?,
    onSelect: (Node) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable(onClick = { onSelect(node) }),
        title = {
            ListItemTitleText(
                text = if (node.url == getGemNodeUrl(chain)) {
                    "Gem Wallet Node"
                } else {
                    node.url.replace("https://", "").replace("http://", "")
                },
                titleBadge = { NodeItemStatus(nodeStatus) }
            )
        },
        subtitle = {
            Text(
                text = "${stringResource(R.string.nodes_import_node_latest_block)} - ${nodeStatus?.blockNumber ?: ""}",
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        trailing = if (selected) {
            @Composable {
                Icon(
                    modifier = Modifier.Companion.padding(end = padding8).size(20.dp),
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else null
    )
}

@Composable
private fun NodeItemStatus(nodeStatus: NodeStatus?) {
    if (nodeStatus?.loading == true) {
        Spacer6()
        CircularProgressIndicator14()
        return
    }
    val color = when {
        nodeStatus?.loading == true -> Color.Transparent
        nodeStatus?.inSync == true -> when {
            nodeStatus.latency < 1024 -> MaterialTheme.colorScheme.tertiary
            nodeStatus.latency < 2048 -> Color(0xffff9314)
            else -> MaterialTheme.colorScheme.error
        }
        else -> MaterialTheme.colorScheme.error
    }
    Row(
        Modifier
            .padding(start = 5.dp)
            .background(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val text = if (nodeStatus?.inSync == true) {
            stringResource(R.string.common_latency_in_ms, nodeStatus.latency)
        } else {
            stringResource(R.string.errors_error)
        }
        Text(
            modifier = Modifier.padding(start = 5.dp, top = 2.dp, end = padding4, bottom = 2.dp),
            text = text,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Preview
@Composable
fun NodeItemPreview() {
    WalletTheme {
        NodeItem(
            chain = Chain.Ethereum,
            node = Node(
                url = "some.url.eth",
                status = NodeState.Active,
                priority = 0,
            ),
            selected = true,
            nodeStatus = NodeStatus(
                url = "",
                chainId = Chain.Ethereum.string,
                blockNumber = "123902302938",
                inSync = true,
                latency = 440,
                loading = false,
            )
        ) { }
    }
}