package com.gemwallet.android.features.settings.price_alerts.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.ext.same
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.components.ActionIcon
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SwipeableItemWithActions
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.padding16
import com.wallet.core.primitives.AssetId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertScene(
    alertingPrice: List<AssetItemUIModel>,
    enabled: Boolean,
    syncState: Boolean,
    onEnablePriceAlerts: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onExclude: (AssetId) -> Unit,
    onChart: (AssetId) -> Unit,
    onRefresh: () -> Unit,
    onCancel: () -> Unit,
) {
    var reveableAssetId = remember { mutableStateOf<AssetId?>(null) }
    val pullToRefreshState = rememberPullToRefreshState()
    Scene(
        title = stringResource(R.string.settings_price_alerts_title),
        actions = @Composable {
            IconButton(onClick = onAdd) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "")
            }
        },
        onClose = onCancel
    ) {
        val isRefreshing = syncState
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = padding16),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.settings_enable_value, "")
                    )
                    Switch(
                        checked = enabled,
                        onCheckedChange = { onEnablePriceAlerts(it) },
                    )
                }
                Text(
                    modifier = Modifier.padding(horizontal = padding16),
                    text = stringResource(R.string.price_alerts_get_notified_explain_message),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            emptyAlertingAssets(alertingPrice.isEmpty())
            assets(
                reveableAssetId = reveableAssetId,
                assets = alertingPrice,
                onChart = onChart,
                onExclude = onExclude,
            )
        }
    }
        }
}

private fun LazyListScope.emptyAlertingAssets(empty: Boolean) {
    if (!empty) {
        return
    }
    item {
        Spacer16()
        Spacer16()
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.price_alerts_empty_state_message),
            textAlign = TextAlign.Center,
        )
        Spacer16()
    }
}

private fun LazyListScope.assets(
    reveableAssetId: MutableState<AssetId?>,
    assets: List<AssetItemUIModel>,
    onChart: (AssetId) -> Unit,
    onExclude: (AssetId) -> Unit,
) {
    items(assets, key = { it.asset.id.toIdentifier()}) { item ->
        var minActionWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current

        SwipeableItemWithActions(
            isRevealed = reveableAssetId.value?.same(item.asset.id) == true,
            actions = @Composable {
                ActionIcon(
                    modifier = Modifier.widthIn(min = minActionWidth),
                    onClick = { onExclude(item.asset.id) },
                    backgroundColor = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.Delete,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            onExpanded = { reveableAssetId.value = item.asset.id },
            onCollapsed = { reveableAssetId.value = null },
        ) {
            Box(
                modifier = Modifier
                    .clickable(onClick = { onChart(item.asset.id) })
                    .onSizeChanged {
                        minActionWidth = with (density) { it.height.toDp() }
                    }
            ) {
                AssetListItem(item)
            }
        }
    }
}

@Preview
@Composable
fun PriceAlertScreenPreview() {
    WalletTheme {
        PriceAlertScene(
            alertingPrice = emptyList(),
            enabled = true,
            syncState = false,
            onEnablePriceAlerts = {},
            onAdd = {},
            onCancel = {},
            onExclude = {},
            onRefresh = {},
            onChart = {}
        )
    }
}