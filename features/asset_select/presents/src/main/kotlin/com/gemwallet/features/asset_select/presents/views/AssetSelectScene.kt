package com.gemwallet.features.asset_select.presents.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.SearchBar
import com.gemwallet.android.ui.components.TabsBar
import com.gemwallet.android.ui.components.filters.AssetsFilter
import com.gemwallet.android.ui.components.image.IconWithBadge
import com.gemwallet.android.ui.components.list_item.AssetInfoUIModel
import com.gemwallet.android.ui.components.list_item.AssetItemUIModel
import com.gemwallet.android.ui.components.list_item.AssetListItem
import com.gemwallet.android.ui.components.list_item.PinnedAssetsHeaderItem
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.property.itemsPositioned
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.models.AssetsGroupType
import com.gemwallet.android.ui.theme.defaultPadding
import com.gemwallet.android.ui.theme.paddingDefault
import com.gemwallet.android.ui.theme.paddingHalfSmall
import com.gemwallet.android.ui.theme.paddingSmall
import com.gemwallet.android.ui.theme.trailingIconMedium
import com.gemwallet.features.asset_select.viewmodels.models.UIState
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetTag
import com.wallet.core.primitives.Chain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
fun AssetSelectScene(
    title: String,
    popular: ImmutableList<AssetItemUIModel>,
    pinned: ImmutableList<AssetItemUIModel>,
    unpinned: ImmutableList<AssetItemUIModel>,
    recent: ImmutableList<AssetItemUIModel>,
    state: UIState,
    titleBadge: (AssetItemUIModel) -> String?,
    support: ((AssetItemUIModel) -> (@Composable () -> Unit)?)?,
    query: TextFieldState,
    tags: List<AssetTag?>,
    selectedTag: AssetTag?,
    isAddAvailable: Boolean = false,
    availableChains: List<Chain> = emptyList(),
    chainsFilter: List<Chain> = emptyList(),
    balanceFilter: Boolean = false,
    searchable: Boolean = true,
    onChainFilter: (Chain) -> Unit,
    onBalanceFilter: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onSelect: ((AssetId) -> Unit)?,
    onTagSelect: (AssetTag?) -> Unit,
    onCancel: () -> Unit,
    itemTrailing: (@Composable (AssetItemUIModel) -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    onAddAsset: (() -> Unit)? = null,
) {
    AssetSelectScene(
        title = {
            Text(
                modifier = Modifier,
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        popular = popular,
        pinned = pinned,
        unpinned = unpinned,
        recent = recent,
        state = state,
        titleBadge = titleBadge,
        support = support,
        query = query,
        tags = tags,
        selectedTag = selectedTag,
        isAddAvailable = isAddAvailable,
        availableChains = availableChains,
        chainsFilter = chainsFilter,
        balanceFilter = balanceFilter,
        onChainFilter = onChainFilter,
        onBalanceFilter = onBalanceFilter,
        onClearFilters = onClearFilters,
        onSelect = onSelect,
        onTagSelect = onTagSelect,
        onCancel = onCancel,
        itemTrailing = itemTrailing,
        actions = actions,
        onAddAsset = onAddAsset,
    )
}

@Composable
fun AssetSelectScene(
    title: @Composable () -> Unit,
    popular: ImmutableList<AssetItemUIModel>,
    pinned: ImmutableList<AssetItemUIModel>,
    unpinned: ImmutableList<AssetItemUIModel>,
    recent: ImmutableList<AssetItemUIModel>,
    state: UIState,
    titleBadge: (AssetItemUIModel) -> String?,
    support: ((AssetItemUIModel) -> (@Composable () -> Unit)?)?,
    query: TextFieldState,
    tags: List<AssetTag?>,
    selectedTag: AssetTag?,
    isAddAvailable: Boolean = false,
    availableChains: List<Chain> = emptyList(),
    chainsFilter: List<Chain> = emptyList(),
    balanceFilter: Boolean = false,
    searchable: Boolean = true,
    onChainFilter: (Chain) -> Unit,
    onBalanceFilter: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onSelect: ((AssetId) -> Unit)?,
    onTagSelect: (AssetTag?) -> Unit,
    onCancel: () -> Unit,
    itemTrailing: (@Composable (AssetItemUIModel) -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    onAddAsset: (() -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isReturnToTop by remember { mutableStateOf(false) }

    var showSelectNetworks by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        coroutineScope.launch {
            snapshotFlow { query.text.toString() }.collect {
                isReturnToTop = it.isEmpty()
            }
        }
    }

    LaunchedEffect(pinned, unpinned) {
        if (isReturnToTop) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
            isReturnToTop = false
        }
    }

    Scene(
        titleContent = title,
        actions = {
            IconButton(onClick = { showSelectNetworks = !showSelectNetworks }) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    tint = if (chainsFilter.isEmpty() && !balanceFilter)
                        LocalContentColor.current
                    else
                        MaterialTheme.colorScheme.primary,
                    contentDescription = "Filter by networks",
                )
            }
            actions()
        },
        onClose = onCancel
    ) {
        if (searchable) {
            SearchBar(query = query)
        }
        LazyColumn(state = listState) {
            item {
                TabsBar(tags, selectedTag, onTagSelect) { item ->
                    val stringId = when (item) {
                        AssetTag.Trending -> R.string.assets_tags_trending
                        AssetTag.TrendingFiatPurchase -> R.string.assets_tags_trending
                        AssetTag.Gainers -> R.string.assets_tags_gainers
                        AssetTag.Losers -> R.string.assets_tags_losers
                        AssetTag.New -> R.string.assets_tags_new
                        AssetTag.Stablecoins -> R.string.assets_tags_stablecoins
                        null -> R.string.common_all
                    }
                    Text(
                        stringResource(stringId),
                    )
                }
            }
            recent(recent, onSelect)
            assets(popular, AssetsGroupType.Popular, onSelect, support, titleBadge, itemTrailing)
            assets(pinned, AssetsGroupType.Pined, onSelect, support, titleBadge, itemTrailing)
            assets(unpinned, AssetsGroupType.None, onSelect, support, titleBadge, itemTrailing)
            loading(state)
            notFound(state = state, onAddAsset = onAddAsset, isAddAvailable = isAddAvailable)
        }
    }

    if (showSelectNetworks) {
        AssetsFilter(
            availableChains = availableChains,
            chainFilter = chainsFilter,
            balanceFilter = balanceFilter,
            onDismissRequest = { showSelectNetworks = false },
            onChainFilter = onChainFilter,
            onBalanceFilter = onBalanceFilter,
            onClearFilters = onClearFilters
        )
    }
}

private fun LazyListScope.assets(
    items: List<AssetItemUIModel>,
    group: AssetsGroupType,
    onSelect: ((AssetId) -> Unit)?,
    support: ((AssetItemUIModel) -> (@Composable () -> Unit)?)?,
    titleBadge: (AssetItemUIModel) -> String?,
    itemTrailing: (@Composable (AssetItemUIModel) -> Unit)?,
) {
    if (items.isEmpty()) return

    item { PinnedAssetsHeaderItem(group) }

    itemsPositioned(items, key = { index, item -> "${item.asset.id.toIdentifier()}-${group.name}" }) { position, item ->
        AssetListItem(
            modifier = Modifier
                .heightIn(74.dp)
                .clickable { onSelect?.invoke(item.asset.id) },
            listPosition = position,
            asset = item,
            support = support?.invoke(item),
            badge = titleBadge.invoke(item),
            trailing = { itemTrailing?.invoke(item) },
        )
    }
}

private fun LazyListScope.notFound(
    state: UIState,
    isAddAvailable: Boolean = false,
    onAddAsset: (() -> Unit)? = null,
) {
    if (state !is UIState.Empty) {
        return
    }
    item {
        Box(
            modifier = Modifier
                .animateItem()
                .fillMaxWidth()
                .defaultPadding(),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = stringResource(id = R.string.assets_no_assets_found))
                if (isAddAvailable) {
                    TextButton(onClick = { onAddAsset?.invoke() }) {
                        Text(text = stringResource(id = R.string.assets_add_custom_token))
                    }
                }
            }
        }
    }
}

private fun LazyListScope.loading(state: UIState) {
    if (state !is UIState.Loading) {
        return
    }
    item {
        Box(
            modifier = Modifier
                .animateItem()
                .fillMaxWidth()
                .defaultPadding(),
        ) {
            CircularProgressIndicator16(Modifier.align(Alignment.Center))
        }
    }
}

private fun LazyListScope.recent(
    items: List<AssetItemUIModel>,
    onSelect: ((AssetId) -> Unit)?
) {
    if (items.isEmpty()) {
        return
    }
    item {
        SubheaderItem(R.string.recent_activity_title)
    }
    item {
        LazyRow(
            modifier = Modifier.padding(top = paddingHalfSmall, start = paddingDefault, bottom = paddingSmall, end = paddingDefault),
            horizontalArrangement = Arrangement.spacedBy(paddingSmall),
        ) {
            items(items) { item ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(onClick = { onSelect?.invoke(item.asset.id) })
                        .padding(paddingSmall),
                    horizontalArrangement = Arrangement.spacedBy(paddingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconWithBadge(item.asset, size = trailingIconMedium)
                    Text(item.asset.symbol)
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewAssetScreenUI() {
    MaterialTheme {
        AssetSelectScene(
            pinned = emptyList<AssetInfoUIModel>().toImmutableList(),
            unpinned = emptyList<AssetInfoUIModel>().toImmutableList(),
            popular = emptyList<AssetInfoUIModel>().toImmutableList(),
            recent = emptyList<AssetInfoUIModel>().toImmutableList(),
            state = UIState.Idle,
            title = "Send",
            titleBadge = { it.asset.symbol },
            support = null,
            tags = AssetTag.entries,
            selectedTag = null,
            query = rememberTextFieldState(),
            onSelect = {},
            onAddAsset = {},
            onChainFilter = {},
            onBalanceFilter = {},
            onClearFilters = {},
            onCancel = {},
            onTagSelect = {},
        )
    }
}