package com.gemwallet.features.asset_select.viewmodels.models

import com.gemwallet.android.model.AssetInfo
import kotlinx.coroutines.flow.Flow

interface SelectSearch {
    fun items(filters: Flow<SelectAssetFilters?>): Flow<List<AssetInfo>>

    fun filter(items: List<AssetInfo>): List<AssetInfo> = items
}