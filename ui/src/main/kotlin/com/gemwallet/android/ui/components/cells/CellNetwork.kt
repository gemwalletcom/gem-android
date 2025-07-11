package com.gemwallet.android.ui.components.cells

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.CellEntity
import com.gemwallet.android.ui.components.designsystem.trailingIconMedium
import com.gemwallet.android.ui.components.image.AsyncImage
import com.gemwallet.android.ui.components.list_item.DataBadgeChevron
import com.gemwallet.android.ui.components.list_item.PropertyDataText
import com.gemwallet.android.ui.components.list_item.PropertyItem
import com.gemwallet.android.ui.components.list_item.PropertyTitleText
import com.wallet.core.primitives.Chain

@Composable
fun cellNetwork(chain: Chain, onOpenNetwork: ((Chain) -> Unit)? = null): CellEntity<String> {
    val asset = chain.asset()
    return CellEntity(
        label = stringResource(id = R.string.transfer_network),
        data = asset.name,
        trailing = { AsyncImage(asset, trailingIconMedium) },
        action = onOpenNetwork?.let { { onOpenNetwork(chain) } },
    )
}

@Composable
fun propertyNetwork(chain: Chain, onOpenNetwork: ((Chain) -> Unit)? = null) {
    val asset = chain.asset()
    PropertyItem(
        modifier = Modifier.clickable { onOpenNetwork?.invoke(chain) },
        title = { PropertyTitleText(R.string.transfer_network, trailing = { AsyncImage(asset, trailingIconMedium) }) },
        data = { PropertyDataText(asset.name, badge = { DataBadgeChevron() }) }
    )
}