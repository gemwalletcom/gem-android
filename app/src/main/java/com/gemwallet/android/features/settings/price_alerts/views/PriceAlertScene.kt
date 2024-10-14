package com.gemwallet.android.features.settings.price_alerts.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.gemwallet.android.R
import com.gemwallet.android.ext.same
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.ActionIcon
import com.gemwallet.android.ui.components.AssetListItem
import com.gemwallet.android.ui.components.Scene
import com.gemwallet.android.ui.components.SwipeableItemWithActions
import com.gemwallet.android.ui.theme.Spacer16
import com.gemwallet.android.ui.theme.WalletTheme
import com.gemwallet.android.ui.theme.padding16
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId

@Composable
fun PriceAlertScene(
    alertingPrice: List<AssetUIState>,
    onAdd: () -> Unit,
    onExclude: (AssetId) -> Unit,
    onChart: (AssetId) -> Unit,
    onCancel: () -> Unit,
) {
    var reveableAssetId = remember { mutableStateOf<AssetId?>(null) }
    Scene(
        title = stringResource(R.string.settings_price_alerts_title),
        actions = @Composable {
            IconButton(onClick = onAdd) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "")
            }
        },
        padding = PaddingValues(horizontal = padding16),
        onClose = onCancel
    ) {
        LazyColumn {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.settings_enable_value, "")
                    )
                    Switch(
                        checked = false,
                        onCheckedChange = {},
                    )
                }
                Text(
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
    }
}

private fun LazyListScope.assets(
    reveableAssetId: MutableState<AssetId?>,
    assets: List<AssetUIState>,
    onChart: (AssetId) -> Unit,
    onExclude: (AssetId) -> Unit,
) {
    items(assets, key = { it.asset.id.toIdentifier()}) { item ->
        SwipeableItemWithActions(
            isRevealed = reveableAssetId.value?.same(item.asset.id) == true,
            actions = @Composable {
                ActionIcon(
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
                modifier = Modifier.clickable(onClick = { onChart(item.asset.id) })
            ) {
                AssetListItem(
                    assetId = item.asset.id,
                    title = item.asset.name,
                    iconUrl = item.asset.getIconUrl(),
                    iconModifier = Modifier,
                    value = item.value,
                    assetType = item.asset.type,
                    isZeroValue = item.isZeroValue,
                    fiatAmount = item.fiat,
                    price = item.price,
                )
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
            onAdd = {},
            onCancel = {},
            onExclude = {},
            onChart = {}
        )
    }
}