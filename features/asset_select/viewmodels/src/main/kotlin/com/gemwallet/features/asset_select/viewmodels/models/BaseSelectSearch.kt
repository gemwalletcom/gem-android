package com.gemwallet.features.asset_select.viewmodels.models

import com.gemwallet.android.data.repositoreis.assets.AssetsRepository
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.model.AssetInfo
import com.gemwallet.android.model.Session
import com.wallet.core.primitives.AssetTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseSelectSearch(
    private val assetsRepository: AssetsRepository,
) : SelectSearch {
    override fun invoke(
        session: Flow<Session?>,
        query: Flow<String>,
        tag: Flow<AssetTag?>,
    ): Flow<List<AssetInfo>> {
        return combine(query, tag) { query, tags ->
            Pair(query, tags)
        }.flatMapLatest {
            assetsRepository.search(it.first, it.second?.let { listOf(it) } ?: emptyList(), false)
        }
        .map { it.distinctBy { it.asset.id.toIdentifier() } }
        .flowOn(Dispatchers.IO)
    }
}