package com.gemwallet.android.features.asset_select.models

import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseSelectSearch(
    private val assetsRepository: AssetsRepository,
) : SelectSearch {
    override fun invoke(
        session: Flow<Session?>,
        query: Flow<String>
    ): Flow<List<AssetInfo>> {
        return query.flatMapLatest { assetsRepository.search(it, false) }
            .map { it.distinctBy { it.asset.id.toIdentifier() } }
            .flowOn(Dispatchers.IO)
    }
}