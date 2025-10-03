package com.gemwallet.android.ui.components.list_item.property

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gemwallet.android.domains.asset.chain
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ui.R
import com.gemwallet.android.domains.asset.getIconUrl
import com.gemwallet.android.domains.asset.isMemoSupport
import com.gemwallet.android.domains.asset.subtype
import com.gemwallet.android.ext.type
import com.gemwallet.android.ui.models.ListPosition
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.AssetSubtype
import com.wallet.core.primitives.Chain

@Composable
fun PropertyNetworkItem(
    chain: Chain,
    value: String = chain.asset().name,
    listPosition: ListPosition = ListPosition.Single,
    onOpenNetwork: AssetIdAction? = null
) {
    val asset = chain.asset()
    PropertyItem(
        modifier = onOpenNetwork?.let {
            Modifier.clickable { it(AssetId(chain)) }
        } ?: Modifier,
        title = { PropertyTitleText(R.string.transfer_network) },
        data = {
            PropertyDataText(
                text = value,
                badge = { DataBadgeChevron(asset.chain.getIconUrl(), onOpenNetwork != null) }
            )
        },
        listPosition = listPosition,
    )
}

@Composable
fun PropertyNetworkItem(
    asset: Asset,
    listPosition: ListPosition = ListPosition.Single,
    onOpenNetwork: AssetIdAction? = null
) {
    PropertyNetworkItem(
        chain = asset.chain,
        value = when (asset.subtype) {
            AssetSubtype.NATIVE -> asset.id.chain.asset().name
            AssetSubtype.TOKEN -> "${asset.id.chain.asset().name} (${asset.type.string})"
        },
        listPosition = listPosition,
        onOpenNetwork = onOpenNetwork
    )
}