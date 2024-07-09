package com.gemwallet.android.features.asset_select.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gemwallet.android.R
import com.gemwallet.android.features.asset_select.components.ItemBalanceTrailing
import com.wallet.core.primitives.AssetId

@Composable
fun SelectSendScreen(
    onCancel: () -> Unit,
    onSelect: ((AssetId) -> Unit)?,
) {
    AssetSelectScreen(
        title = stringResource(id = R.string.wallet_send),
        titleBadge = { null },
//        TODO: predicate = { assetInfo ->
//            assetInfo.balances.available().atomicValue > BigInteger.ZERO
//        },
        itemTrailing = { ItemBalanceTrailing(it) },
        onSelect = onSelect,
        onCancel = onCancel,
    )
}