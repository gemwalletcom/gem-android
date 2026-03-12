package com.gemwallet.android.application.assets.coordinators

import com.gemwallet.android.domains.asset.aggregates.AssetInfoDataAggregate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
interface GetActiveAssetsInfo {
    fun getAssetsInfo(hideBalance: Boolean): Flow<List<AssetInfoDataAggregate>>

    fun getAssetsInfo(hideBalance: Flow<Boolean>): Flow<List<AssetInfoDataAggregate>>
       = hideBalance.flatMapLatest { getAssetsInfo(it) }
}