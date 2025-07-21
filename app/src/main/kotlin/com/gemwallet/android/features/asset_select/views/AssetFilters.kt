package com.gemwallet.android.features.asset_select.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.assetType
import com.gemwallet.android.features.asset_select.components.SearchBar
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.ChainItem
import com.gemwallet.android.ui.components.designsystem.Spacer16
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.components.list_item.SwitchRow
import com.gemwallet.android.ui.components.screen.ModalBottomSheet
import com.wallet.core.primitives.Chain

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AssetFilters(
    chainFilter: List<Chain>,
    balanceFilter: Boolean,
    onDismissRequest: () -> Unit,
    onChainFilter: (Chain) -> Unit,
    onBalanceFilter: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
) {
    var query = rememberTextFieldState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = {
            Row (
                modifier = Modifier.fillMaxWidth().
                padding(padding16),
            ) {
                TextButton(
                    onClick = onClearFilters,
                ) { Text(stringResource(R.string.filter_clear)) }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        modifier = Modifier.padding(vertical = 0.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Box(Modifier.size(width = 32.dp, height = 4.dp))
                    }
                    Spacer8()
                    Text(
                        text = stringResource(R.string.filter_title),
                        textAlign = TextAlign.Center,
                        modifier = Modifier,
                    )
                }
                TextButton(
                    onClick = onDismissRequest,
                ) { Text(stringResource(R.string.common_done)) }
            }
        },
    ) {
        Column(modifier = Modifier.Companion.fillMaxSize()) {
            SearchBar(query, Modifier.Companion.padding(horizontal = padding16))
            Spacer16()
            SwitchRow(
                text = stringResource(R.string.filter_has_balance),
                checked = balanceFilter,
                onCheckedChange = onBalanceFilter,
            )
            LazyColumn(modifier = Modifier.Companion.fillMaxSize()) {
                item {
                    SubheaderItem(stringResource(R.string.settings_networks_title))
                    Spacer8()
                }
                Chain.entries
                    .map { it.asset() }
                    .filter { asset ->
                        val query = query.text.toString().lowercase()
                        asset.name.lowercase().contains(query)
                                || asset.symbol.lowercase().contains(query)
                                || (asset.id.chain.assetType()?.string?.lowercase()
                            ?.contains(query) == true)
                    }
                    .forEach {
                        val chain = it.id.chain
                        item {
                            ChainItem(
                                title = chain.asset().name,
                                icon = chain,
                                trailing = {
                                    if (chainFilter.contains(chain)) {
                                        Icon(
                                            Icons.Default.CheckCircleOutline,
                                            contentDescription = ""
                                        )
                                    }
                                }
                            ) { onChainFilter(chain) }
                        }
                    }
            }
        }
    }
}