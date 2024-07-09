package com.gemwallet.android.features.asset_select.components

import androidx.compose.runtime.Composable
import com.gemwallet.android.features.assets.model.AssetUIState
import com.gemwallet.android.ui.components.getBalanceInfo

@Composable
internal fun ItemBalanceTrailing(
    asset: AssetUIState
) {
    getBalanceInfo(asset.isZeroValue, asset.value, asset.fiat)()
}