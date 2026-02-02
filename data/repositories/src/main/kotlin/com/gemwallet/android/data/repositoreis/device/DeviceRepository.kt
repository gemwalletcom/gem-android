package com.gemwallet.android.data.repositoreis.device

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gemwallet.android.application.device.coordinators.GetDeviceId
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.cases.device.GetDeviceIdOld
import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.GetPushToken
import com.gemwallet.android.cases.device.GetSupportId
import com.gemwallet.android.cases.device.RequestPushToken
import com.gemwallet.android.cases.device.SetPushToken
import com.gemwallet.android.cases.device.SwitchPushEnabled
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.cases.session.GetCurrentCurrencyCase
import com.gemwallet.android.data.repositoreis.config.UserConfig.Keys
import com.gemwallet.android.data.repositoreis.pricealerts.PriceAlertRepository
import com.gemwallet.android.data.service.store.ConfigStore
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.gemwallet.android.data.services.gemapi.GemDeviceApiClient
import com.gemwallet.android.ext.model
import com.gemwallet.android.ext.os
import com.wallet.core.primitives.ChainAddress
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.MigrateDeviceIdRequest
import com.wallet.core.primitives.Platform
import com.wallet.core.primitives.PlatformStore
import com.wallet.core.primitives.Subscription
import com.wallet.core.primitives.SupportDeviceRequest
import com.wallet.core.primitives.Wallet
import com.wallet.core.primitives.WalletSource
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
import wallet.core.jni.AnyAddress
import java.util.Locale
import java.util.UUID
import kotlin.math.max

class DeviceRepository(
    private val context: Context,
    private val gemApiClient: GemApiClient,
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
    SyncSubscription,
    GetSupportId
{
    private val Context.dataStore by preferencesDataStore(name = "device_config")

    init {
        coroutineScope.launch {
            migrate()

            syncDeviceInfo()

            syncSupportInfo()
        }
    }

    private suspend fun migrate() {
        try {
            if (getDeviceIdOld.isMigrated()) return

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

    override fun getSupportId(): Flow<String?> {
        return context.dataStore.data
            .map { preferences -> preferences[Key.SupportId] }
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
        syncOldSubscription(wallets, added)
        val localSubscriptions = localSubscriptions(wallets)
        val remoteSubscriptions = getRemoteSubscriptions()

        val toRemove = remoteSubscriptions.filter { remote ->
            localSubscriptions.firstOrNull { it.wallet_id == remote.wallet_id && it.subscriptions.size == remote.chains.size } == null
        }.map { WalletSubscription(wallet_id = it.wallet_id, WalletSource.Create, subscriptions = emptyList()) }

        val toAdd = localSubscriptions.filter { local ->
            remoteSubscriptions.firstOrNull { it.wallet_id == local.wallet_id && local.subscriptions.size == it.chains.size } == null
        }

        addSubscriptions(toAdd)
        removeSubscriptions(toRemove)

        if (toAdd.isNotEmpty() || toRemove.isNotEmpty()) {
            increaseSubscriptionVersion()
            syncDeviceInfo()
        }
    }

    suspend fun syncOldSubscription(wallets: List<Wallet>, added: Boolean) {
        val deviceId = getDeviceId.getDeviceId()
        val oldDeviceId = getDeviceIdOld.getDeviceId()
        val subscriptionsIndex = buildSubscriptionIndex(wallets)
        val remoteSubscriptions = getOldRemoteSubscriptions(oldDeviceId)

        val toAddSubscriptions = subscriptionsIndex.toMutableMap()
        remoteSubscriptions.forEach {
            toAddSubscriptions.remove("${it.chain.string}_${it.address}_${it.wallet_index}")
        }

        val toRemoveSubscription = if (added) {
            emptyList()
        } else {
            remoteSubscriptions.filter {
                !subscriptionsIndex.contains("${it.chain.string}_${it.address}_${it.wallet_index}")
            }
        }

        addOldSubscriptions(oldDeviceId, toAddSubscriptions.values.toList())
        removeOldSubscriptions(oldDeviceId, toRemoveSubscription)

        if (toAddSubscriptions.isNotEmpty() || toRemoveSubscription.isNotEmpty()) {
            increaseSubscriptionVersion()
            syncDeviceInfo()
        }
    }

    private suspend fun syncSupportInfo() {
        if (!getSupportId().firstOrNull().isNullOrEmpty()) {
            return
        }
        val supportId = UUID.randomUUID().toString().substring(0, 31)
        try {
            gemDeviceApiClient.registerSupport(
                request = SupportDeviceRequest(
                    supportDeviceId = supportId,
                )
            )
            context.dataStore.edit { it[Key.SupportId] = supportId }
        } catch (_: Throwable) { }
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

    private suspend fun removeSubscriptions(subscriptions: List<WalletSubscription>) {
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
                wallet_id = wallet.id,
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

    private suspend fun getOldRemoteSubscriptions(deviceId: String): List<Subscription> {
        return try {
            gemApiClient.getOldSubscriptions(deviceId) ?: throw Exception()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun addOldSubscriptions(deviceId: String, subscriptions: List<Subscription>) {
        if (subscriptions.isEmpty()) {
            return
        }
        try {
            gemApiClient.addOldSubscriptions(deviceId, subscriptions)
        } catch (_: Throwable) { }
    }

    private suspend fun removeOldSubscriptions(deviceId: String, subscriptions: List<Subscription>) {
        if (subscriptions.isEmpty()) {
            return
        }
        try {
            gemApiClient.deleteOldSubscriptions(deviceId, subscriptions)
        } catch (_: Throwable) { }
    }

    private fun buildSubscriptionIndex(wallets: List<Wallet>): Map<String, Subscription> {
        val subscriptionsIndex = mutableMapOf<String, Subscription>()
        wallets.forEach { wallet ->
            wallet.accounts.forEach { account ->
                val checksum = AnyAddress(account.address, WCChainTypeProxy().invoke(account.chain)).description()
                subscriptionsIndex["${account.chain.string}_${checksum}_${wallet.index}"] = Subscription(
                    chain = account.chain,
                    address = checksum,
                    wallet_index = wallet.index,
                )
            }
        }
        return subscriptionsIndex
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
        PushEnabled("push_enabled"),
        PushToken("push_token"),
        ;
    }

    private object Key {
        val PushEnabled = booleanPreferencesKey("push_enabled")
        val SupportId = stringPreferencesKey("support-id")
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