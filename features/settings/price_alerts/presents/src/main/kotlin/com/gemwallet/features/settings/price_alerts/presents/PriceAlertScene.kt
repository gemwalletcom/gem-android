package com.gemwallet.features.settings.price_alerts.presents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.unit.dp
import com.gemwallet.android.domains.pricealerts.aggregates.PriceAlertDataAggregate
import com.gemwallet.android.domains.pricealerts.aggregates.PriceAlertType
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.ActionIcon
import com.gemwallet.android.ui.components.list_item.Badge
import com.gemwallet.android.ui.components.list_item.ListItem
import com.gemwallet.android.ui.components.list_item.ListItemTitleText
import com.gemwallet.android.ui.components.list_item.PriceInfo
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.SwipeableItemWithActions
import com.gemwallet.android.ui.components.list_item.SwitchProperty
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.headerIconSize
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.paddingLarge
import com.gemwallet.android.ui.theme.paddingSmall
import com.wallet.core.primitives.AssetId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceAlertScene(
    assetId: AssetId? = null,
    data: Map<AssetId?, List<PriceAlertDataAggregate>>,
    enabled: Boolean,
    syncState: Boolean,
    isAssetView: Boolean,
    onEnablePriceAlerts: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onAddTarget: (AssetId) -> Unit,
    onExclude: (Int) -> Unit,
    onChart: (AssetId) -> Unit,
    onRefresh: () -> Unit,
    onCancel: () -> Unit,
) {
    val reveable = remember { mutableStateOf<Int?>(null) }
    val pullToRefreshState = rememberPullToRefreshState()
    Scene(
        title = stringResource(R.string.settings_price_alerts_title),
        actions = @Composable {
            IconButton(onClick = if (assetId == null) onAdd else {
                { onAddTarget(assetId) }
            }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "")
            }
        },
        onClose = onCancel
    ) {
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            isRefreshing = syncState,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = syncState,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.background
                )
            }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (!isAssetView) {
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
                }
                emptyAlertingAssets(data.values.flatten().isEmpty())
                assets(
                    reveable = reveable,
                    data = data,
                    isAssetView = isAssetView,
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
        Box(modifier = Modifier.fillMaxWidth().fillParentMaxHeight(0.7f)) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(paddingHalfSmall),
            ) {
                Icon(
                    modifier = Modifier
                        .size(headerIconSize)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), CircleShape)
                        .padding(paddingSmall),
                    imageVector = Icons.Default.Notifications,
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = ""
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.price_alerts_state_empty_title),
                    textAlign = TextAlign.Center,
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondary,
                    text = stringResource(R.string.price_alerts_state_empty_description),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun LazyListScope.assets(
    reveable: MutableState<Int?>,
    data: Map<AssetId?, List<PriceAlertDataAggregate>>,
    isAssetView: Boolean,
    onChart: (AssetId) -> Unit,
    onExclude: (Int) -> Unit,
) {
    data.entries.forEach { item ->
        if (item.value.isEmpty()) return@forEach

        item.key?.let {
            item { SubheaderItem(if (isAssetView) stringResource(R.string.stake_active) else item.value.firstOrNull()?.title ?: "") }
        }
        assets(reveable, item.value, onChart.takeIf { !isAssetView }, onExclude)
    }
}

private fun LazyListScope.assets(
    reveable: MutableState<Int?>,
    data: List<PriceAlertDataAggregate>,
    onChart: ((AssetId) -> Unit)?,
    onExclude: (Int) -> Unit,
) {
    itemsPositioned(data/*, key = { _, item -> item.id}*/) { position, item ->
        var minActionWidth by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current

        SwipeableItemWithActions(
            isRevealed = reveable.value == item.id,
            actions = @Composable {
                ActionIcon(
                    modifier = Modifier
                        .widthIn(min = minActionWidth)
                        .heightIn(minActionWidth),
                    onClick = { onExclude(item.id) },
                    backgroundColor = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.Delete,
                )
            },
            onExpanded = { reveable.value = item.id },
            onCollapsed = { reveable.value = null },
            listPosition = position,
        ) { position ->
            ListItem(
                modifier = (onChart?.let { Modifier
                    .clickable(onClick = { onChart(item.assetId) }) } ?: Modifier)
                    .onSizeChanged {
                        minActionWidth = with(density) { it.height.toDp() }
                    },
                listPosition = position,
                leading = @Composable { IconWithBadge(item.icon) },
                title = @Composable { ListItemTitleText(item.title, { Badge(text = item.titleBadge) }) },
                subtitle = {
                    val (price, changes) = when (item.type) {
                        PriceAlertType.Auto -> Pair(item.price, item.percentage)
                        PriceAlertType.Over -> Pair(stringResource(R.string.price_alerts_direction_over), item.price)
                        PriceAlertType.Under -> Pair(stringResource(R.string.price_alerts_direction_under), item.price)
                        PriceAlertType.Increase -> Pair(stringResource(R.string.price_alerts_direction_increases_by), item.percentage)
                        PriceAlertType.Decrease -> Pair(stringResource(R.string.price_alerts_direction_decreases_by), item.percentage)
                    }
                    PriceInfo(price, changes, item.priceState)
                },
            )
        }
    }
}