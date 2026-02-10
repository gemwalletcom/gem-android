package com.gemwallet.android.data.repositoreis.device

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.gemwallet.android.application.device.coordinators.GetDeviceId
import com.gemwallet.android.cases.device.GetDeviceIdOld
import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.GetPushToken
import com.gemwallet.android.cases.device.RequestPushToken
import com.gemwallet.android.cases.device.SetPushToken
import com.gemwallet.android.cases.device.SwitchPushEnabled
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.cases.session.GetCurrentCurrencyCase
import com.gemwallet.android.data.repositoreis.config.UserConfig.Keys
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.service.store.ConfigStore
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import com.gemwallet.android.ext.model
import com.gemwallet.android.ext.os
import com.wallet.core.primitives.ChainAddress
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.MigrateDeviceIdRequest
import com.wallet.core.primitives.Platform
import com.wallet.core.primitives.PlatformStore
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletSubscription
import com.wallet.core.primitives.WalletSubscriptionChains
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max

class DeviceRepository(
    private val context: Context,
    private val gemDeviceApiClient: GemDeviceApiClient,
    private val configStore: ConfigStore,
    private val requestPushToken: RequestPushToken,
    private val platformStore: PlatformStore,
    private val versionName: String,
    private val getDeviceIdOld: GetDeviceIdOld,
    private val getDeviceId: GetDeviceId,
    private val priceAlertRepository: PriceAlertRepository,
    private val getCurrentCurrencyCase: GetCurrentCurrencyCase,
    coroutineScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + CoroutineExceptionHandler { _, _ -> /*Log.e("DEVICE", "Err:", err)*/ } + Dispatchers.IO
    ),
) : SyncDeviceInfo,
    SwitchPushEnabled,
    GetPushEnabled,
    GetPushToken,
    SetPushToken,
    SyncSubscription
{
    private val Context.dataStore by preferencesDataStore(name = "device_config")

    init {
        coroutineScope.launch {
            migrate()

            syncDeviceInfo()
        }
    }

    private suspend fun migrate() {
        try {
            if (getDeviceIdOld.isMigrated()) return

            if (getDeviceIdOld.getDeviceId().isEmpty()) return

            gemDeviceApiClient.migrateDevice(
                MigrateDeviceIdRequest(
                    oldDeviceId = getDeviceIdOld.getDeviceId(),
                    publicKey = getDeviceId.getDeviceId()
                )
            )
            getDeviceIdOld.migrated()
        } catch (_: Throwable) { }
    }

    override suspend fun syncDeviceInfo() {
        val pushToken = getPushToken()
        val pushEnabled = getPushEnabled().firstOrNull() ?: false
        val device = Device(
            id = getDeviceId.getDeviceId(),
            platform = Platform.Android,
            platformStore = platformStore,
            os = Platform.os,
            model = Platform.model,
            token = pushToken,
            locale = getLocale(Locale.getDefault()),
            isPushEnabled = pushEnabled,
            isPriceAlertsEnabled = priceAlertRepository.isPriceAlertsEnabled().firstOrNull(),
            version = versionName,
            currency = getCurrentCurrencyCase.getCurrentCurrency().string,
            subscriptionsVersion = getSubscriptionVersion(),
        )
        if (pushEnabled && pushToken.isEmpty()) {
            requestPushToken.requestToken { pushToken ->
                setPushToken(pushToken)
                CoroutineScope(Dispatchers.IO).launch {
                    handlePushToken(pushToken, device)
                }
            }
        } else {
            handlePushToken(pushToken, device)
        }
    }

    override suspend fun switchPushEnabled(enabled: Boolean, wallets: List<Wallet>) {
        context.dataStore.edit { preferences ->
            preferences[Key.PushEnabled] = enabled
        }
        try {
            syncDeviceInfo()
            syncSubscription(wallets)
        } catch (_: Throwable) {}
    }

    override fun getPushEnabled(): Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Key.PushEnabled] == true }

    override fun setPushToken(token: String) {
        configStore.putString(ConfigKey.PushToken.string, token)
    }

    override suspend fun getPushToken(): String {
        return if (getPushEnabled().firstOrNull() == true) {
            configStore.getString(ConfigKey.PushToken.string)
        } else {
            ""
        }
    }

    override suspend fun syncSubscription(wallets: List<Wallet>, added: Boolean) { // TODO: WalletID Migration: remove when back will ready
        val localSubscriptions = localSubscriptions(wallets)
        val remoteSubscriptions = getRemoteSubscriptions()

        val toRemove = remoteSubscriptions.filter { remote ->
            localSubscriptions.firstOrNull { it.walletId == remote.walletId && it.subscriptions.size == remote.chains.size } == null
        }.map { WalletSubscriptionChains(walletId = it.walletId, chains = it.chains) }

        val toAdd = localSubscriptions.filter { local ->
            remoteSubscriptions.firstOrNull { it.walletId == local.walletId && local.subscriptions.size == it.chains.size } == null
        }

        addSubscriptions(toAdd)
        removeSubscriptions(toRemove)

        if (toAdd.isNotEmpty() || toRemove.isNotEmpty()) {
            increaseSubscriptionVersion()
            syncDeviceInfo()
        }
    }

    private suspend fun handlePushToken(pushToken: String, device: Device) {
        val device = device.copy(token = pushToken)

        if (isDeviceRegistered()) {
            updateDevice(device)
        } else {
            registerDevice(device)
        }
    }

    private suspend fun isDeviceRegistered(): Boolean {
        val local = context.dataStore.data.map { it[Key.DeviceRegistered] }.firstOrNull() == true
        return local || gemDeviceApiClient.isDeviceRegistered()
    }

    private suspend fun registerDevice(device: Device) = try {
        gemDeviceApiClient.registerDevice(device)
        val isRegistered = (gemDeviceApiClient.isDeviceRegistered())
        setDeviceRegistered(isRegistered)
    } catch (_: Throwable) {}

    private suspend fun updateDevice(device: Device) {
        try {
            val remote = gemDeviceApiClient.getDevice()

            if (remote?.hasChanges(device) == true) {
                val subscriptionsVersion = max(device.subscriptionsVersion, remote.subscriptionsVersion) + 1
                setSubscriptionVersion(subscriptionsVersion)
                gemDeviceApiClient.updateDevice(request = device.copy(subscriptionsVersion = subscriptionsVersion))
                setDeviceRegistered(true)
            }
        } catch (_: Throwable) { }
    }

    private suspend fun setDeviceRegistered(isRegistered: Boolean = true) {
        context.dataStore.edit { it[Key.DeviceRegistered] = isRegistered }
    }

    private suspend fun getRemoteSubscriptions(): List<WalletSubscriptionChains> {
        return try {
            gemDeviceApiClient.getSubscriptions() ?: throw Exception()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun addSubscriptions(subscriptions: List<WalletSubscription>) {
        if (subscriptions.isEmpty()) {
            return
        }
        try {
            gemDeviceApiClient.addSubscriptions(subscriptions)
        } catch (_: Throwable) { }
    }

    private suspend fun removeSubscriptions(subscriptions: List<WalletSubscriptionChains>) {
        if (subscriptions.isEmpty()) {
            return
        }
        try {
            gemDeviceApiClient.deleteSubscriptions(subscriptions)
        } catch (_: Throwable) { }
    }

    private fun localSubscriptions(wallets: List<Wallet>): List<WalletSubscription> {
        return wallets.map { wallet ->
            val subscriptions =  wallet.accounts.map { account ->
                ChainAddress(account.chain, account.address)
            }
            WalletSubscription(
                walletId = wallet.id,
                source = wallet.source,
                subscriptions = subscriptions
            )
        }
    }

    private fun getSubscriptionVersion(): Int {
        return configStore.getInt(Keys.SubscriptionVersion.string)
    }

    private fun setSubscriptionVersion(subVersion: Int) {
        configStore.putInt(
            Keys.SubscriptionVersion.string,
            subVersion
        )
    }

    private fun increaseSubscriptionVersion() {
        val newVersion = getSubscriptionVersion() + 1
        configStore.putInt(
            Keys.SubscriptionVersion.string,
            newVersion
        )
    }

    private fun Device.hasChanges(other: Device): Boolean {
        return id != other.id
                || token != other.token
                || locale != other.locale
                || isPushEnabled != other.isPushEnabled
                || version != other.version
                || subscriptionsVersion != other.subscriptionsVersion
                || isPriceAlertsEnabled != other.isPriceAlertsEnabled
    }

    internal enum class ConfigKey(val string: String) {
        DeviceId("device-uuid"),
        PushToken("push_token"),
        ;
    }

    private object Key {
        val PushEnabled = booleanPreferencesKey("push_enabled")
        val DeviceRegistered = booleanPreferencesKey("device_registered")
    }

    companion object {
        fun getLocale(locale: Locale): String {
            val tag = locale.toLanguageTag()
            if (tag == "pt-BR" || tag == "pt_BR") {
                return "pt-BR"
            }
            if (locale.language == "zh") {
                return "${locale.language}-${(locale.script.ifEmpty { "Hans" })}"
            }
            return  locale.language
        }
    }
}