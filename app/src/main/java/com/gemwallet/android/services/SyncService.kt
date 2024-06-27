package com.gemwallet.android.services

import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.config.NodesRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.interactors.sync.SyncAvailableToBuy
import com.gemwallet.android.interactors.sync.SyncConfig
import com.gemwallet.android.interactors.sync.SyncDevice
import com.gemwallet.android.interactors.sync.SyncNodes
import com.gemwallet.android.interactors.sync.SyncSubscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncService @Inject constructor(
    private val gemApiClient: GemApiClient,
    private val configRepository: ConfigRepository,
    private val nodesRepository: NodesRepository,
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
) {

    private val operators = listOf(
        SyncNodes(nodesRepository),
        SyncAvailableToBuy(gemApiClient, configRepository),
        SyncSubscription(gemApiClient, walletsRepository, configRepository),
    )

    suspend fun sync() {
        withContext(Dispatchers.IO) {
            listOf(
                async { SyncConfig(gemApiClient, configRepository).invoke() },
                async { SyncDevice(gemApiClient, configRepository, sessionRepository).invoke() }
            ).awaitAll()
            operators.map {
                async { it() }
            }.awaitAll()
        }
    }
}