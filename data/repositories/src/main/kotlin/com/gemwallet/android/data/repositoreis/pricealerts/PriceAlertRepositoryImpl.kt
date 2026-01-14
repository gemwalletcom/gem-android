package com.gemwallet.android.data.repositoreis.pricealerts

import com.gemwallet.android.cases.device.GetDeviceId
import com.gemwallet.android.cases.pricealerts.EnablePriceAlert
import com.gemwallet.android.cases.pricealerts.GetPriceAlerts
import com.gemwallet.android.cases.pricealerts.PutPriceAlert
import com.gemwallet.android.data.service.store.ConfigStore
import com.gemwallet.android.data.service.store.database.PriceAlertsDao
import com.gemwallet.android.data.service.store.database.entities.DbPriceAlert
import com.gemwallet.android.data.service.store.database.entities.toDTO
import com.gemwallet.android.data.service.store.database.entities.toModels
import com.gemwallet.android.data.service.store.database.entities.toRecord
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PriceAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PriceAlertRepositoryImpl(
    private val gemClient: GemApiClient,
    private val priceAlertsDao: PriceAlertsDao,
    private val getDeviceId: GetDeviceId,
    private val configStore: ConfigStore,
) :
    PriceAlertRepository,
    GetPriceAlerts, PutPriceAlert, EnablePriceAlert {

    override suspend fun addPriceAlert(priceAlert: PriceAlert) {
        priceAlertsDao.put(listOf(priceAlert.toRecord()))
    }

    override fun getPriceAlerts(): Flow<List<PriceAlert>> {
        return priceAlertsDao.getAlerts().toModels()
    }

    override fun getPriceAlert(assetId: AssetId): Flow<PriceAlert?> {
        return priceAlertsDao.getAlert(assetId.toIdentifier()).filterNotNull().toDTO()
    }

    override suspend fun setAssetPriceAlertEnabled(assetId: AssetId, currency: Currency, enabled: Boolean): Unit = withContext(Dispatchers.IO) {
        val assetIdentifier = assetId.toIdentifier()
        val priceAlert = priceAlertsDao.getAlert(assetIdentifier).firstOrNull().let {
            if (it == null) {
                priceAlertsDao.put(listOf(DbPriceAlert(assetId = assetIdentifier, currency = currency.string, enabled = true)))
                listOf(PriceAlert(assetId, currency.string))
            } else {
                listOf(it.toDTO())
            }
        }
        priceAlertsDao.enabled(assetIdentifier, enabled)

        if (enabled) {
            setPriceAlertEnabled(true)
        }

        launch(Dispatchers.IO) {
            try {
                if (enabled) {
                    gemClient.includePriceAlert(getDeviceId(), priceAlert)
                } else {
                    gemClient.excludePriceAlert(getDeviceId(), priceAlert)
                }
            } catch (_: Throwable) { }
            sync()
        }
    }

    override suspend fun putPriceAlert(alert: PriceAlert) = withContext(Dispatchers.IO) {
        priceAlertsDao.put(listOf(alert.toRecord()))
        launch(Dispatchers.IO) {
            try {
                gemClient.includePriceAlert(getDeviceId(), listOf(alert))
            } catch (_: Throwable) {}
        }
        Unit
    }

    override fun isAssetPriceAlertEnabled(assetId: AssetId): Flow<Boolean> {
        return priceAlertsDao.getAlert(assetId.toIdentifier())
            .map { it != null && it.enabled }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun setPriceAlertEnabled(enabled: Boolean) {
        configStore.putBoolean(
            ConfigKey.PriceAlertsEnabled.string,
            enabled
        )
    }

    override fun isPriceAlertEnabled(): Boolean {
        return configStore.getBoolean(ConfigKey.PriceAlertsEnabled.string)
    }

    private suspend fun sync() {
        try {
            gemClient.includePriceAlert(
                getDeviceId(),
                getPriceAlerts().firstOrNull() ?: emptyList()
            )
        } catch (_: Throwable) {}
        val local = priceAlertsDao.getAlerts().firstOrNull() ?: emptyList()
        val remote = try {
            gemClient.getPriceAlerts(getDeviceId())
        } catch (_: Throwable) {
            return
        }
        val toExclude = remote.filter { remote ->
            local.firstOrNull { it.assetId.toAssetId() == remote.assetId } == null
        }
        try {
            gemClient.excludePriceAlert(getDeviceId(), toExclude)
        } catch (_: Throwable) { }
    }

    private fun getDeviceId() = getDeviceId.getDeviceId()

    private enum class ConfigKey(val string: String) {
        PriceAlertsEnabled("price_alerts_enabled"),
        ;
    }
}