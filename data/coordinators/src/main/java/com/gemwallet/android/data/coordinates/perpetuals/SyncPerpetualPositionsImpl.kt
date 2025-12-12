package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.SyncPerpetualPositions
import com.gemwallet.android.blockchain.services.PerpetualService
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.PerpetualPositionsSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncPerpetualPositionsImpl @Inject constructor(
    private val perpetualService: PerpetualService,
    private val sessionRepository: SessionRepository,
    private val perpetualRepository: PerpetualRepository,
    private val chains: List<Chain> = listOf(Chain.HyperCore),
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : SyncPerpetualPositions {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val syncFlow = sessionRepository.session()
        .filterNotNull()
        .map { it.wallet.accounts.filter { chains.contains(it.chain) } }
        .flatMapLatest { accounts ->
            flow {
                while (true) {
                    val summaries = withContext(Dispatchers.IO) {
                        accounts.map {
                                async {
                                    Pair(it.address, perpetualService.getPositions(it.chain, it.address))
                                }
                            }
                            .awaitAll()
                    }
                    emit(summaries)
                    delay(5 * 1000)
                }
            }
        }
        .onEach { items ->
            items.forEach { item ->
                val summary = item.second
                val accountAddress = item.first
                perpetualRepository.putPositions(accountAddress, summary?.positions ?: return@onEach)
                perpetualRepository.putBalance(accountAddress, summary.balance)
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, emptyList())


    override suspend fun syncPerpetualPositions() {
    }
}