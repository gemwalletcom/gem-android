package com.gemwallet.android.application.wallet.coordinators

import com.gemwallet.android.domains.wallet.aggregates.WalletDataAggregate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

interface GetAllWallets {
    fun getAllWallets(): Flow<List<WalletDataAggregate>>
}

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<List<WalletDataAggregate>>.pinned(): Flow<List<WalletDataAggregate>> = this.mapLatest { items -> items.filter { it.isPinned } }

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<List<WalletDataAggregate>>.unpinned(): Flow<List<WalletDataAggregate>> = this.mapLatest { items -> items.filter { !it.isPinned } }