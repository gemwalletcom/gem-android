package com.gemwallet.features.settings.price_alerts.presents

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.ActionIcon
import com.gemwallet.android.ui.components.list_item.AssetItemUIModel
import com.gemwallet.android.ui.components.list_item.AssetListItem
import com.gemwallet.android.ui.components.list_item.PriceInfo
import com.gemwallet.android.ui.components.list_item.SwipeableItemWithActions
import com.gemwallet.android.ui.components.list_item.SwitchProperty
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.paddingLarge
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
    val reveableAssetId = remember { mutableStateOf<AssetId?>(null) }
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
                    SwitchProperty(
                        text = stringResource(R.string.settings_enable_value, ""),
                        checked = enabled,
                        onCheckedChange = onEnablePriceAlerts
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = paddingLarge),
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
            text = stringResource(R.string.price_alerts_state_empty_title),
            textAlign = TextAlign.Center,
        )
        // TODO: Add empty description
        Spacer16()
    }
}

private fun LazyListScope.assets(
    reveableAssetId: MutableState<AssetId?>,
    assets: List<AssetItemUIModel>,
    onChart: (AssetId) -> Unit,
    onExclude: (AssetId) -> Unit,
) {
    itemsPositioned(assets, key = { index, item -> item.asset.id.toIdentifier()}) { position, item ->
        var minActionWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current

        SwipeableItemWithActions(
            isRevealed = reveableAssetId.value == item.asset.id,
            actions = @Composable {
                ActionIcon(
                    modifier = Modifier.widthIn(min = minActionWidth).heightIn(minActionWidth),
                    onClick = { onExclude(item.asset.id) },
                    backgroundColor = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.Delete,
                )
            },
            onExpanded = { reveableAssetId.value = item.asset.id },
            onCollapsed = { reveableAssetId.value = null },
            listPosition = position,
        ) { position ->
            AssetListItem(
                modifier = Modifier
                    .clickable(onClick = { onChart(item.asset.id) })
                    .onSizeChanged {
                        minActionWidth = with (density) { it.height.toDp() }
                        Log.d("PRICE_ALER", "Min width: $minActionWidth")
                    },
                asset = item,
                support = {
                    PriceInfo(
                        price = item.price,
                        style = MaterialTheme.typography.bodyMedium,
                        internalPadding = 4.dp
                    )
                },
                listPosition = position
            )
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