package com.gemwallet.android.services

import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.cases.transactions.SyncTransactions
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncService @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
    private val syncTransactions: SyncTransactions,
    private val buyRepository: BuyRepository,
    private val syncSubscription: SyncSubscription,
) {

    suspend fun sync() = withContext(Dispatchers.IO) {
        listOf(
            async { syncTransactions.syncTransactions(sessionRepository.session().firstOrNull()?.wallet ?: return@async) },
            async { buyRepository.sync() }
        ).awaitAll()
        syncSubscription.syncSubscription(walletsRepository.getAll().firstOrNull() ?: emptyList())
    }
}