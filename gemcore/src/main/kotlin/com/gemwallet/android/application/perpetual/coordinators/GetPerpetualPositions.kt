package com.gemwallet.android.application.perpetual.coordinators

import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDataAggregate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
interface GetPerpetualPositions {

    fun getPerpetualPositions(searchQuery: Flow<String?>): Flow<List<PerpetualDataAggregate>> {
        return searchQuery.flatMapLatest { getPerpetualPositions(it) }
    }

    fun getPerpetualPositions(query: String? = null): Flow<List<PerpetualDataAggregate>>
}