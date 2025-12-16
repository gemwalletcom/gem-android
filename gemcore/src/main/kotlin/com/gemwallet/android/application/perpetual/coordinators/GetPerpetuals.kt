package com.gemwallet.android.application.perpetual.coordinators

import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDataAggregate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
interface GetPerpetuals {

    fun getPerpetuals(searchQuery: Flow<String?>): Flow<List<PerpetualDataAggregate>> {
        return searchQuery.flatMapLatest { getPerpetuals(it) }
    }

    fun getPerpetuals(searchQuery: String? = null): Flow<List<PerpetualDataAggregate>>
}