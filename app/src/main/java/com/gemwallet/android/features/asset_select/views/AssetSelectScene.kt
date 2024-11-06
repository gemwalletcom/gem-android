package com.gemwallet.android.features.asset_select.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import com.gemwallet.android.features.asset_select.viewmodels.AssetSelectViewModel
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.models.AssetInfoUIModel
import com.gemwallet.android.ui.models.AssetItemUIModel
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.padding16
import com.wallet.core.primitives.AssetId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun AssetSelectScene(
    title: String,
    assets: ImmutableList<AssetItemUIModel>,
    state: AssetSelectViewModel.UIState,
    titleBadge: (AssetItemUIModel) -> String?,
    support: ((AssetItemUIModel) -> String?)?,
    query: TextFieldState,
    onSelect: ((AssetId) -> Unit)?,
    onCancel: () -> Unit,
    itemTrailing: (@Composable (AssetItemUIModel) -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    onAddAsset: (() -> Unit)? = null,
) {
    val items by remember(assets) { mutableStateOf(assets) }
    Scene(title = title, actions = actions, onClose = onCancel) {
        SearchBar(modifier = Modifier.padding(horizontal = padding16), query = query)
        Spacer16()
        LazyColumn {
            assets(items, onSelect, support, titleBadge, itemTrailing)
            loading(state)
            notFound(state = state, onAddAsset = onAddAsset)
        }
    }
}

private fun LazyListScope.assets(
    items: List<AssetItemUIModel>,
    onSelect: ((AssetId) -> Unit)?,
    support: ((AssetItemUIModel) -> String?)?,
    titleBadge: (AssetItemUIModel) -> String?,
    itemTrailing: (@Composable (AssetItemUIModel) -> Unit)?,
) {
    if (items.isEmpty()) {
        return
    }
    items(items.size, key = { items[it].asset.id.toIdentifier() }) { index ->
        val item = items[index]
        AssetListItem(
            modifier = Modifier
                .heightIn(74.dp)
                .clickable { onSelect?.invoke(item.asset.id) },
            uiModel = item,
            support = support?.invoke(item),
            badge = titleBadge.invoke(item),
            trailing = { itemTrailing?.invoke(item) },
        )
    }
}

private fun LazyListScope.notFound(
    state: AssetSelectViewModel.UIState,
    onAddAsset: (() -> Unit)? = null,
) {
    if (state !is AssetSelectViewModel.UIState.Empty) {
        return
    }
    item {
        Box(
            modifier = Modifier
                .animateItem()
                .fillMaxWidth()
                .padding(padding16)
        ) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(text = stringResource(id = R.string.assets_no_assets_found))
                TextButton(onClick = { onAddAsset?.invoke() }) {
                    Text(text = stringResource(id = R.string.assets_add_custom_token))
                }
            }
        }
    }
}

private fun LazyListScope.loading(state: AssetSelectViewModel.UIState) {
    if (state !is AssetSelectViewModel.UIState.Loading) {
        return
    }
    item {
        Box(modifier = Modifier.animateItem().fillMaxWidth().padding(padding16)) {
            CircularProgressIndicator16(Modifier.align(Alignment.Center))
        }
    }
}

@Composable
@Preview
fun PreviewAssetScreenUI() {
    MaterialTheme {
        AssetSelectScene(
            assets = emptyList<AssetInfoUIModel>().toImmutableList(),
            state = AssetSelectViewModel.UIState.Idle,
            title = "Send",
            titleBadge = { it.asset.symbol },
            support = null,
            query = rememberTextFieldState(),
            onSelect = {},
            onAddAsset = {},
            onCancel = {},
        )
    }
}