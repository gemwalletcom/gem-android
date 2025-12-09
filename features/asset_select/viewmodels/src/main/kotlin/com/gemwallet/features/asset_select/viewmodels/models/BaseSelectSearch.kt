package com.gemwallet.features.asset_select.viewmodels.models

import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseSelectSearch(
    private val assetsRepository: AssetsRepository,
) : SelectSearch {

    override fun items(filters: Flow<SelectAssetFilters?>): Flow<List<AssetInfo>> {
        return filters.flatMapLatest { filters ->
            assetsRepository.search(filters?.query ?: "", filters?.tag?.let { listOf(it) } ?: emptyList(), false)
        }.map { items -> items.distinctBy { it.asset.id.toIdentifier() } }
    }
}