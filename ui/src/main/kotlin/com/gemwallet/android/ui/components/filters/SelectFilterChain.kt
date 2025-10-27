package com.gemwallet.android.ui.components.filters

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.assetType
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.list_item.ChainItem
import com.gemwallet.android.ui.components.list_item.SubheaderItem
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.theme.paddingSmall
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
    val items = availableChains.map { it.asset() }.filter { asset ->
        val query = query.lowercase()
        asset.name.lowercase().contains(query)
                || asset.symbol.lowercase().contains(query)
                || (asset.id.chain.assetType()?.string?.lowercase()
            ?.contains(query) == true)
    }
    val size = items.size
    items.forEachIndexed { index, item ->
        val chain = item.id.chain
        item {
            ChainItem(
                title = chain.asset().name,
                icon = chain,
                listPosition = ListPosition.getPosition(index, size),
                trailing = {
                    if (chainFilter.contains(chain)) {
                        Icon(
                            modifier = Modifier.Companion.padding(end = paddingSmall).size(20.dp),
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            ) { onFilter(chain) }
        }
    }
}