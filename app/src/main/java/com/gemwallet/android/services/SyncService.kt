package com.gemwallet.android.services

import android.content.Context
import android.telephony.TelephonyManager
import androidx.fragment.app.FragmentActivity.TELEPHONY_SERVICE
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.config.NodesRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.wallet.WalletsRepository
import com.gemwallet.android.interactors.sync.SyncAvailableToBuy
import com.gemwallet.android.interactors.sync.SyncConfig
import com.gemwallet.android.interactors.sync.SyncDevice
import com.gemwallet.android.interactors.sync.SyncNodes
import com.gemwallet.android.interactors.sync.SyncSubscription
import com.gemwallet.android.interactors.sync.SyncTransactions
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
    private val syncTransactions: SyncTransactions,
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
                async { SyncDevice(gemApiClient, configRepository, sessionRepository).invoke() },
                async { syncTransactions(sessionRepository.getSession()?.wallet?.index ?: return@async) },
            ).awaitAll()
            operators.map {
                async { it() }
            }.awaitAll()
        }
    }
}

fun isAvailableOperation(context: Context): Boolean = (context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager)
    .networkCountryIso.let {
        !(it == "uk" || it == "gb")
    }