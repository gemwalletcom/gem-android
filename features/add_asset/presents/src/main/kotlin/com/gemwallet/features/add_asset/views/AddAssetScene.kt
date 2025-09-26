package com.gemwallet.features.add_asset.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.buttons.MainActionButton
import com.gemwallet.android.ui.components.list_item.ChainItem
import com.gemwallet.android.ui.components.list_item.property.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.property.PropertyDataText
import com.gemwallet.android.ui.components.list_item.property.PropertyItem
import com.gemwallet.android.ui.components.list_item.property.PropertyTitleText
import com.gemwallet.android.ui.components.progress.CircularProgressIndicator16
import com.gemwallet.android.ui.components.screen.Scene
import com.gemwallet.android.ui.theme.defaultPadding
import com.gemwallet.features.add_asset.viewmodels.models.TokenSearchState
import com.wallet.core.primitives.Asset

@Composable
fun AddAssetScene(
    searchState: TokenSearchState,
    addressState: MutableState<String>,
    network: Asset,
    token: Asset?,
    onScan: () -> Unit,
    onAddAsset: () -> Unit,
    onChainSelect: (() -> Unit)?,
    onCancel: () -> Unit,
) {
    Scene(
        title = stringResource(id = R.string.wallet_add_token_title),
        mainAction = {
            MainActionButton(
                title = stringResource(id = R.string.wallet_import_action),
                enabled = searchState is TokenSearchState.Idle && token != null,
                onClick = onAddAsset,
            )
        },
        onClose = onCancel,
    ) {
        ChainItem(
            modifier = Modifier.height(64.dp),
            title = network.name,
            icon = network.chain,
            onClick = onChainSelect,
            dividerShowed = true,
            trailing = if (onChainSelect != null) {
                {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ChevronRight),
                        contentDescription = "open_provider_select",
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            } else null
        )
        Column(modifier = Modifier.defaultPadding()) {
            AddressChainField(
                chain = network.chain,
                label = stringResource(R.string.wallet_import_contract_address_field),
                value = addressState.value,
                searchName = false,
                onValueChange = { input, _ ->
                    addressState.value = input
                },
                onQrScanner = onScan,
            )
        }
        if (searchState is TokenSearchState.Loading) {
            Box {
                CircularProgressIndicator16(modifier = Modifier.align(Alignment.Center))
            }
        }
        if (searchState is TokenSearchState.Error) {
            Box {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultPadding(),
                    text = stringResource(id = R.string.errors_token_unable_fetch_token_information, addressState.value),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
        }
        AssetInfoTable(token)
    }
}

@Composable
private fun ColumnScope.AssetInfoTable(asset: Asset?) {
    if (asset == null) {
        return
    }
    PropertyItem(
        title = { PropertyTitleText(R.string.asset_name) },
        data = { PropertyDataText(asset.name, badge = { DataBadgeChevron(asset, false) }) }
    )
    PropertyItem(R.string.asset_symbol, asset.symbol)
    PropertyItem(R.string.asset_decimals, asset.decimals.toString())
}