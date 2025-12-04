package com.gemwallet.features.asset_select.viewmodels.models

import com.gemwallet.android.model.AssetInfo
import kotlinx.coroutines.flow.Flow

fun interface SelectSearch {
    operator fun invoke(filters: Flow<SelectAssetFilters?>): Flow<List<AssetInfo>>
}