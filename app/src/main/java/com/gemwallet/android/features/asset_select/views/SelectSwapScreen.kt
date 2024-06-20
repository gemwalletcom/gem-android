package com.gemwallet.android.features.asset_select.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.R
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.features.asset_select.components.itemBalanceTrailing
import com.gemwallet.android.features.swap.model.SwapItemType
import com.gemwallet.android.features.swap.model.SwapScreenState
import com.gemwallet.android.model.AssetInfo
import com.wallet.core.primitives.AssetId

@Composable
fun SelectSwapScreen(
    select: SwapScreenState.Select,
    onCancel: () -> Unit,
    onSelect: ((AssetId, SwapItemType) -> Unit)?,
) {
    val predicate: (AssetInfo) -> Boolean = remember(select.prevAssetId?.toIdentifier(), select.oppositeAssetId) {
        { select.predicate(it) }
    }
    AssetSelectScreen(
        title = when (select.changeType) {
            SwapItemType.Pay -> stringResource(id = R.string.swap_you_pay)
            SwapItemType.Receive -> stringResource(id = R.string.swap_you_receive)
        },
        titleBadge = { null },
        itemTrailing = { itemBalanceTrailing(it) },
        predicate = predicate,
        onSelect = { onSelect?.invoke(it, select.changeType) },
        onCancel = onCancel,
    )
}