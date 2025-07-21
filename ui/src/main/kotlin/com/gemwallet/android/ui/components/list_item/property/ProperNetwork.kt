package com.gemwallet.android.ui.components.list_item.property

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.chain
import com.gemwallet.android.ui.R
import com.gemwallet.android.ui.components.image.getIconUrl
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Chain

@Composable
fun PropertyNetwork(chain: Chain, value: String = chain.asset().name, onOpenNetwork: AssetIdAction? = null) {
    val asset = chain.asset()
    PropertyItem(
        modifier = onOpenNetwork?.let {
            Modifier.clickable { it(AssetId(chain)) }
        } ?: Modifier,
        title = { PropertyTitleText(R.string.transfer_network) },
        data = {
            PropertyDataText(
                text = value,
                badge = { DataBadgeChevron(asset.chain().getIconUrl(), onOpenNetwork != null) }
            )
        },
    )
}

@Composable
fun PropertyNetwork(asset: Asset, onOpenNetwork: AssetIdAction? = null) {
    PropertyNetwork(asset.chain(), "${asset.id.chain.asset().name} (${asset.type.string})", onOpenNetwork)
}