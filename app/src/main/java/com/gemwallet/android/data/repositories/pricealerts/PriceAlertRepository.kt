package com.gemwallet.android.data.repositories.pricealerts

import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
import com.gemwallet.android.cases.pricealerts.GetPriceAlertsCase
import com.gemwallet.android.cases.pricealerts.PutPriceAlertCase
import com.gemwallet.android.data.database.PriceAlertsDao
import com.gemwallet.android.data.database.entities.DbPriceAlert
import com.gemwallet.android.data.database.mappers.PriceAlertMapper
import com.gemwallet.android.data.repositories.config.ConfigRepository
import com.gemwallet.android.data.repositories.config.ConfigStore
import com.gemwallet.android.ext.toIdentifier
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.AssetId
import com.wallet.core.primitives.PriceAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.map

class PriceAlertRepository(
    private val gemClient: GemApiClient,
    private val priceAlertsDao: PriceAlertsDao,
    private val configRepository: ConfigRepository,
    private val configStore: ConfigStore,
) : GetPriceAlertsCase, PutPriceAlertCase, EnablePriceAlertCase {

    private val mapper = PriceAlertMapper()

    override fun getPriceAlerts(): Flow<List<PriceAlert>> {
        return priceAlertsDao.getAlerts().map { items -> items.map(mapper::asEntity) }
    }

    override fun getPriceAlert(assetId: AssetId): Flow<PriceAlert?> {
        return priceAlertsDao.getAlert(assetId.toIdentifier()).filterNotNull().map(mapper::asEntity)
    }

    override suspend fun enabled(assetId: AssetId, enabled: Boolean): Unit = withContext(Dispatchers.IO) {
        val assetIdentifier = assetId.toIdentifier()
        val priceAlert = priceAlertsDao.getAlert(assetIdentifier).firstOrNull().let {
            if (it == null) {
                priceAlertsDao.put(listOf(DbPriceAlert(assetIdentifier, enabled = true)))
                listOf(PriceAlert(assetIdentifier))
            } else {
                listOf(mapper.asEntity(it))
            }
        }
        priceAlertsDao.enabled(assetIdentifier, enabled)

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
        priceAlertsDao.put(listOf(mapper.asDomain(alert)))
        launch(Dispatchers.IO) {
            gemClient.includePriceAlert(getDeviceId(), listOf(alert))
        }
        Unit
    }

    override fun enabled(assetId: AssetId): Flow<Boolean> {
        return priceAlertsDao.getAlert(assetId.toIdentifier()).map { it != null && it.enabled }
    }

    fun setPriceAlertsEnabled(enabled: Boolean) {
        configStore.putBoolean(ConfigKey.PriceAlertsEnabled.string, enabled)
    }

    fun isPriceAlertEnabled(): Boolean {
        return configStore.getBoolean(ConfigKey.PriceAlertsEnabled.string)
    }

    private suspend fun sync() {
        gemClient.includePriceAlert(getDeviceId(), getPriceAlerts().firstOrNull() ?: emptyList())
        val local = priceAlertsDao.getAlerts().firstOrNull() ?: emptyList()
        val remote = gemClient.getPriceAlerts(getDeviceId()).getOrNull() ?: return
        val toExclude = remote.filter { remote ->
            local.firstOrNull { it.assetId == remote.assetId } == null
        }
        gemClient.excludePriceAlert(getDeviceId(), toExclude)
    }

    private fun getDeviceId() = configRepository.getDeviceId()

    private enum class ConfigKey(val string: String) {
        PriceAlertsEnabled("price_alerts_enabled"),
        ;
    }
}