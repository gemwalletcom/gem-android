package com.gemwallet.android.features.asset_select.views

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.foundation.text2.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset_select.components.SearchBar
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.features.assets.model.PriceState
import com.gemwallet.android.features.assets.model.PriceUIState
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.CircularProgressIndicator16
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.padding16
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetType
import com.wallet.core.primitives.Chain
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AssetSelectScene(
    title: String,
    titleBadge: (AssetUIState) -> String?,
    support: ((AssetUIState) -> String?)?,
    query: TextFieldState,
    assets: ImmutableList<AssetUIState>,
    loading: Boolean,
    onSelect: ((AssetId) -> Unit)?,
    onCancel: () -> Unit,
    itemTrailing: (@Composable (AssetUIState) -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    onAddAsset: (() -> Unit)? = null,
) {
    val items by remember(assets) {
        mutableStateOf(assets)
    }
    Scene(
        title = title,
        actions = actions,
        onClose = onCancel,
    ) {
        SearchBar(
            modifier = Modifier.padding(horizontal = padding16),
            query = query,
        )
        Spacer16()
        LazyColumn {
            assets(items, onSelect, support, titleBadge, itemTrailing)
            loading(loading = loading)
            notFound(items = items, loading = loading, onAddAsset = onAddAsset)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.assets(
    items: List<AssetUIState>,
    onSelect: ((AssetId) -> Unit)?,
    support: ((AssetUIState) -> String?)?,
    titleBadge: (AssetUIState) -> String?,
    itemTrailing: (@Composable (AssetUIState) -> Unit)?,
) {
    items(items.size, key = { items[it].id.toIdentifier() }) { index ->
        val asset = items[index]
        AssetListItem(
            modifier = Modifier.animateItemPlacement(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = LinearOutSlowInEasing,
                    )
                )
                .heightIn(74.dp)
                .clickable { onSelect?.invoke(asset.id) },
            chain = asset.id.chain,
            title = asset.name,
            support = support?.invoke(asset),
            assetType = asset.type,
            iconUrl = asset.icon,
            badge = titleBadge.invoke(asset),
            trailing = { itemTrailing?.invoke(asset) },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.notFound(
    loading: Boolean,
    items: List<AssetUIState>,
    onAddAsset: (() -> Unit)? = null,
) {
    if (items.isNotEmpty() || loading) {
        return
    }
    item {
        Box(
            modifier = Modifier
                .animateItemPlacement(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = LinearOutSlowInEasing,
                    )
                )
                .fillMaxWidth()
                .padding(padding16)
        ) {
            Column(modifier = Modifier.align(Alignment.Center),) {
                Text(text = stringResource(id = R.string.assets_no_assets_found))
                TextButton(onClick = { onAddAsset?.invoke() }) {
                    Text(text = stringResource(id = R.string.assets_add_custom_token))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.loading(loading: Boolean) {
    if (!loading) {
        return
    }
    item {
        Box(
            modifier = Modifier
                .animateItemPlacement(
                    animationSpec = tween(
                        durationMillis = 150,
                        easing = LinearOutSlowInEasing,
                    )
                )
                .fillMaxWidth()
                .padding(padding16)
        ) {
            CircularProgressIndicator16(Modifier.align(Alignment.Center))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun PreviewAssetScreenUI() {
    MaterialTheme {
        AssetSelectScene(
            assets = listOf(
                AssetUIState(
                    id = AssetId(Chain.Bitcoin),
                    name = "Foo Name",
                    icon = "",
                    symbol = "BTC",
                    type = AssetType.NATIVE,
                    value = "0",
                    isZeroValue = true,
                    fiat = "0",
                    price = PriceUIState(value = "0,0", state = PriceState.Up, dayChanges = "0,1%")
                )
            ).toImmutableList(),
            loading = false,
            title = "Send",
            titleBadge = { it.symbol },
            support = null,
            query = rememberTextFieldState(),
            onSelect = {},
            onAddAsset = {},
            onCancel = {},
        )
    }
}