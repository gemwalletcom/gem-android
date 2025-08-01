package com.gemwallet.android.ui.components.filters

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.assetType
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.designsystem.Spacer8
import com.gemwallet.android.ui.components.list_item.ChainItem
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.wallet.core.primitives.Chain

fun LazyListScope.selectFilterChain(
    availableChains: List<Chain>,
    chainFilter: List<Chain>,
    query: String,
    onFilter: (Chain) -> Unit,
) {
    if (availableChains.size < 2) {
        return
    }
    item {
        SubheaderItem(stringResource(R.string.settings_networks_title))
    }
    availableChains.map { it.asset() }.filter { asset ->
        val query = query.lowercase()
        asset.name.lowercase().contains(query)
                || asset.symbol.lowercase().contains(query)
                || (asset.id.chain.assetType()?.string?.lowercase()
            ?.contains(query) == true)
    }.forEach {
        val chain = it.id.chain
        item {
            ChainItem(
                title = chain.asset().name,
                icon = chain,
                trailing = {
                    if (chainFilter.contains(chain)) {
                        Icon(Icons.Default.CheckCircleOutline, contentDescription = "")
                    }
                }
            ) { onFilter(chain) }
        }
    }
}