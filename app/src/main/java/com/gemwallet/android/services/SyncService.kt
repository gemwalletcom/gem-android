package com.gemwallet.android.services

import android.content.Context
import android.telephony.TelephonyManager
import androidx.fragment.app.FragmentActivity.TELEPHONY_SERVICE
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.data.repositories.buy.BuyRepository
import com.gemwallet.android.data.repositories.config.ConfigRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.data.repositories.wallet.WalletsRepository
import com.gemwallet.android.interactors.sync.SyncDevice
import com.gemwallet.android.interactors.sync.SyncSubscription
import com.gemwallet.android.interactors.sync.SyncTransactions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class SyncService @Inject constructor(
    private val gemApiClient: GemApiClient,
    private val configRepository: ConfigRepository,
    private val sessionRepository: SessionRepository,
    private val walletsRepository: WalletsRepository,
    private val syncTransactions: SyncTransactions,
    private val enablePriceAlertCase: EnablePriceAlertCase,
    private val buyRepository: BuyRepository,
) {

    suspend fun sync() {
        withContext(Dispatchers.IO) {
            listOf(
                async { SyncDevice(gemApiClient, configRepository, sessionRepository, enablePriceAlertCase).invoke() },
                async { syncTransactions(sessionRepository.getSession()?.wallet ?: return@async) },
                async { buyRepository.sync() }
            ).awaitAll()
            SyncSubscription(gemApiClient, walletsRepository, configRepository)()
        }
    }
}

fun isAvailableOperation(context: Context): Boolean = (context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager)
    .networkCountryIso.let {
        !(it.lowercase() == "uk" || it.lowercase() == "gb" || Locale.getDefault().country.lowercase() == "gb")
    }