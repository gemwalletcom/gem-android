package com.gemwallet.android.services

import android.content.Context
import android.telephony.TelephonyManager
import androidx.fragment.app.FragmentActivity.TELEPHONY_SERVICE
import com.gemwallet.android.cases.device.SyncSubscriptionCase
import com.gemwallet.android.data.repositoreis.buy.BuyRepository
import com.gemwallet.android.data.repositoreis.session.SessionRepository
import com.gemwallet.android.data.repositoreis.wallets.WalletsRepository
import com.gemwallet.android.interactors.sync.SyncTransactions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class SyncService @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
    private val syncTransactions: SyncTransactions,
    private val buyRepository: BuyRepository,
    private val syncSubscriptionCase: SyncSubscriptionCase,
) {

    suspend fun sync() {
        withContext(Dispatchers.IO) {
            listOf(
                async { syncTransactions(sessionRepository.getSession()?.wallet ?: return@async) },
                async { buyRepository.sync() }
            ).awaitAll()
            syncSubscriptionCase.syncSubscription(walletsRepository.getAll())
        }
    }
}

fun isAvailableOperation(context: Context): Boolean = (context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager)
    .networkCountryIso.let {
        !(it.lowercase() == "uk" || it.lowercase() == "gb" || Locale.getDefault().country.lowercase() == "gb")
    }