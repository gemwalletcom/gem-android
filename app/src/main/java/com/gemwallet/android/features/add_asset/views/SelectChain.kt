package com.gemwallet.android.features.add_asset.views

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.R
import com.gemwallet.android.ext.asset
import com.gemwallet.android.interactors.getIconUrl
import com.gemwallet.android.ui.components.ChainItem
import com.gemwallet.android.ui.components.Scene
import com.wallet.core.primitives.Chain

@Composable
fun SelectChain(
    chains: List<Chain>,
    onSelect: (Chain) -> Unit,
    onCancel: () -> Unit,
) {
    Scene(
        title = stringResource(id = R.string.transfer_network),
        onClose = onCancel,
    ) {
        LazyColumn(modifier = Modifier) {
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