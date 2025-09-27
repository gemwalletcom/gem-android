package com.gemwallet.features.asset.presents.details.views.components

import androidx.compose.foundation.lazy.LazyListScope
import com.gemwallet.android.ui.components.list_item.property.PropertyNetwork
import com.gemwallet.android.ui.models.actions.AssetIdAction
import com.gemwallet.features.asset.viewmodels.details.models.AssetInfoUIModel
import com.wallet.core.primitives.AssetType

internal fun LazyListScope.network(
    uiState: AssetInfoUIModel,
    openNetwork: AssetIdAction,
) {
    if (uiState.tokenType == AssetType.NATIVE) {
        return
    }
    item { PropertyNetwork(uiState.asset, openNetwork) }
}