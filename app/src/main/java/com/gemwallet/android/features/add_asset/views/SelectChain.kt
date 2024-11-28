package com.gemwallet.android.features.add_asset.views

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.R
import com.gemwallet.android.ext.asset
import com.gemwallet.android.features.asset_select.components.SearchBar
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.components.ChainItem
import com.gemwallet.android.ui.components.designsystem.padding16
import com.gemwallet.android.ui.components.screen.Scene
import com.wallet.core.primitives.Chain

@Composable
fun SelectChain(
    chains: List<Chain>,
    chainFilter: TextFieldState,
    listState: LazyListState = rememberLazyListState(),
    onSelect: (Chain) -> Unit,
    onCancel: () -> Unit,
) {
    Scene(
        title = stringResource(id = R.string.transfer_network),
        onClose = onCancel,
    ) {
        LazyColumn(modifier = Modifier, state = listState) {
            item {
                SearchBar(query = chainFilter, modifier = Modifier.padding(padding16))
            }
            items(chains.size) {
                val chain = chains[it]
                ChainItem(
                    chain = chain,
                    title = chain.asset().name,
                    icon = chain.getIconUrl(),
                ) {
                    onSelect(chain)
                }
            }
        }
    }
}