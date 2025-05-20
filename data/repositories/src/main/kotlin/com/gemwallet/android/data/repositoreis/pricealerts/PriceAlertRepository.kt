package com.gemwallet.android.data.repositoreis.pricealerts

import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.cases.pricealerts.GetPriceAlertsCase
import com.gemwallet.android.cases.pricealerts.PutPriceAlertCase
import com.gemwallet.android.data.service.store.ConfigStore
import com.gemwallet.android.data.service.store.database.PriceAlertsDao
import com.gemwallet.android.data.service.store.database.entities.DbPriceAlert
import com.gemwallet.android.data.service.store.database.entities.toModel
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PriceAlertRepository(
    private val gemClient: GemApiClient,
    private val priceAlertsDao: PriceAlertsDao,
    private val getDeviceIdCase: GetDeviceIdCase,
    private val configStore: ConfigStore,
) : GetPriceAlertsCase, PutPriceAlertCase, EnablePriceAlertCase {

    override fun getPriceAlerts(): Flow<List<PriceAlert>> {
        return priceAlertsDao.getAlerts().toModels()
    }

    override fun getPriceAlert(assetId: AssetId): Flow<PriceAlert?> {
        return priceAlertsDao.getAlert(assetId.toIdentifier()).filterNotNull().toModel()
    }

    override suspend fun setAssetPriceAlertEnabled(assetId: AssetId, enabled: Boolean): Unit = withContext(Dispatchers.IO) {
        val assetIdentifier = assetId.toIdentifier()
        val priceAlert = priceAlertsDao.getAlert(assetIdentifier).firstOrNull().let {
            if (it == null) {
                priceAlertsDao.put(listOf(DbPriceAlert(assetIdentifier, enabled = true)))
                listOf(PriceAlert(assetId, Currency.USD.string)) // TODO: Add user currency select
            } else {
                listOf(it.toModel())
            }
        }
        priceAlertsDao.enabled(assetIdentifier, enabled)

        if (enabled) {
            setPriceAlertEnabled(true)
        }

        launch(Dispatchers.IO) {
            if (enabled) {
                gemClient.includePriceAlert(getDeviceId(), priceAlert)
            } else {
                gemClient.excludePriceAlert(getDeviceId(), priceAlert)
            }
            sync()
        }
    }

    override suspend fun putPriceAlert(alert: PriceAlert) = withContext(Dispatchers.IO) {
        priceAlertsDao.put(listOf(alert.toRecord()))
        launch(Dispatchers.IO) {
            gemClient.includePriceAlert(getDeviceId(), listOf(alert))
        }
        Unit
    }

    override fun isAssetPriceAlertEnabled(assetId: AssetId): Flow<Boolean> {
        return priceAlertsDao.getAlert(assetId.toIdentifier()).map { it != null && it.enabled }
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
        gemClient.includePriceAlert(getDeviceId(), getPriceAlerts().firstOrNull() ?: emptyList())
        val local = priceAlertsDao.getAlerts().firstOrNull() ?: emptyList()
        val remote = gemClient.getPriceAlerts(getDeviceId()).getOrNull() ?: return
        val toExclude = remote.filter { remote ->
            local.firstOrNull { it.assetId.toAssetId() == remote.assetId } == null
        }
        gemClient.excludePriceAlert(getDeviceId(), toExclude)
    }

    private fun getDeviceId() = getDeviceIdCase.getDeviceId()

    private enum class ConfigKey(val string: String) {
        PriceAlertsEnabled("price_alerts_enabled"),
        ;
    }
}