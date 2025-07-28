package com.gemwallet.android.data.repositoreis.device

import android.util.Log
import com.gemwallet.android.blockchain.operators.walletcore.WCChainTypeProxy
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.device.GetPushEnabled
import com.gemwallet.android.cases.device.GetPushToken
import com.gemwallet.android.cases.device.RequestPushToken
import com.gemwallet.android.cases.device.SetPushToken
import com.gemwallet.android.cases.device.SwitchPushEnabled
import com.gemwallet.android.cases.device.SyncDeviceInfo
import com.gemwallet.android.cases.device.SyncSubscription
import com.gemwallet.android.cases.pricealerts.EnablePriceAlert
import com.gemwallet.android.cases.session.GetCurrentCurrencyCase
import com.gemwallet.android.data.repositoreis.config.UserConfig.Keys
import com.gemwallet.android.data.service.store.ConfigStore
import com.gemwallet.android.data.services.gemapi.GemApiClient
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.Platform
import com.wallet.core.primitives.PlatformStore
import com.wallet.core.primitives.Subscription
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import wallet.core.jni.AnyAddress
import java.util.Locale
import kotlin.math.max

class DeviceRepository(
    private val gemApiClient: GemApiClient,
    private val configStore: ConfigStore,
    private val requestPushToken: RequestPushToken,
    private val platformStore: PlatformStore,
    private val versionName: String,
    private val getDeviceIdCase: GetDeviceIdCase,
    private val enablePriceAlert: EnablePriceAlert,
    private val getCurrentCurrencyCase: GetCurrentCurrencyCase,
    coroutineScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + CoroutineExceptionHandler { _, err -> Log.e("DEVICE", "Err:", err) } + Dispatchers.IO
    ),
) : SyncDeviceInfo,
    SwitchPushEnabled,
    GetPushEnabled,
    GetPushToken,
    SetPushToken,
    SyncSubscription
{
    init {
        coroutineScope.launch { syncDeviceInfo() }
    }

    override suspend fun syncDeviceInfo() {
        val pushToken = getPushToken()
        val pushEnabled = pushToken.isNotEmpty()
        val deviceId = getDeviceIdCase.getDeviceId()
        val device = Device(
            id = deviceId,
            platform = Platform.Android,
            platformStore = platformStore,
            token = "",
            locale = getLocale(Locale.getDefault()),
            isPushEnabled = pushEnabled,
            isPriceAlertsEnabled = enablePriceAlert.isPriceAlertEnabled(),
            version = versionName,
            currency = getCurrentCurrencyCase.getCurrentCurrency().string,
            subscriptionsVersion = getSubscriptionVersion(),
        )
        val register: () -> Unit = {
            CoroutineScope(Dispatchers.IO).launch {
                callRegisterDevice(device.copy(token = getPushToken()))
            }
        }
        if (pushEnabled && pushToken.isEmpty()) {
            requestPushToken.invoke { pushToken ->
                setPushToken(pushToken)
                register()
            }
        } else {
            register()
        }
    }

    private suspend fun callRegisterDevice(device: Device) {
        val remoteDeviceInfo = try {
            gemApiClient.getDevice(device.id)
        } catch (_: Throwable) {
            null
        }
        try {
            when {
                remoteDeviceInfo == null -> gemApiClient.registerDevice(device)
                remoteDeviceInfo.hasChanges(device) -> {
                    val subVersion = max(device.subscriptionsVersion, remoteDeviceInfo.subscriptionsVersion) + 1
                    setSubscriptionVersion(subVersion)
                    gemApiClient.updateDevice(
                        device.id,
                        device.copy(subscriptionsVersion = subVersion)
                    )
                }
            }
        } catch (err: Throwable) {
            Log.d("REGISTER-DEVICE", "Error", err)
        }
    }

    override fun switchPushEnabledCase(enabled: Boolean) {
        configStore.putBoolean(ConfigKey.PushEnabled.string, enabled)
    }

    override fun getPushEnabled(): Boolean = configStore.getBoolean(ConfigKey.PushEnabled.string)

    override fun setPushToken(token: String) {
        configStore.putString(ConfigKey.PushToken.string, token)
    }

    override fun getPushToken(): String {
        return if (getPushEnabled()) {
            configStore.getString(ConfigKey.PushToken.string)
        } else {
            ""
        }
    }

    override suspend fun syncSubscription(wallets: List<Wallet>) {
        val deviceId = getDeviceIdCase.getDeviceId()
        val subscriptionsIndex = buildSubscriptionIndex(wallets)
        val remoteSubscriptions = getRemoteSubscriptions(deviceId)

        val toAddSubscriptions = subscriptionsIndex.toMutableMap()
        remoteSubscriptions.forEach {
            toAddSubscriptions.remove("${it.chain.string}_${it.address}_${it.wallet_index}")
        }

        val toRemoveSubscription = remoteSubscriptions.filter {
            !subscriptionsIndex.contains("${it.chain.string}_${it.address}_${it.wallet_index}")
        }

        addSubscriptions(deviceId, toAddSubscriptions.values.toList())
        removeSubscriptions(deviceId, toRemoveSubscription)

        if (toAddSubscriptions.isNotEmpty()) {
            increaseSubscriptionVersion()
            syncDeviceInfo()
        }
    }

    private suspend fun getRemoteSubscriptions(deviceId: String): List<Subscription> {
        return try {
            gemApiClient.getSubscriptions(deviceId) ?: throw Exception()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun addSubscriptions(deviceId: String, subscriptions: List<Subscription>) {
        if (subscriptions.isEmpty()) {
            return
        }
        try {
            gemApiClient.addSubscriptions(deviceId, subscriptions)
        } catch (err: Throwable) {
            Log.d("GEM_API", "Add subscription error: ", err)
        }
    }

    private suspend fun removeSubscriptions(deviceId: String, subscriptions: List<Subscription>) {
        if (subscriptions.isEmpty()) {
            return
        }
        try {
            gemApiClient.deleteSubscriptions(deviceId, subscriptions)
        } catch (err: Throwable) {
            Log.d("GEM_API", "Remove subscription error: ", err)
        }
    }

    private fun buildSubscriptionIndex(wallets: List<Wallet>): Map<String, Subscription> {
        val subscriptionsIndex = mutableMapOf<String, Subscription>()
        wallets.forEach { wallet ->
            wallet.accounts.forEach { account ->
                val checksum = AnyAddress(account.address, WCChainTypeProxy().invoke(account.chain)).description()
                subscriptionsIndex["${account.chain.string}_${account.address}_${wallet.index}"] = Subscription(
                    chain = account.chain,
                    address = checksum,
                    wallet_index = wallet.index,
                )
            }
        }
        return subscriptionsIndex
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
        PushEnabled("push_enabled"),
        PushToken("push_token"),
        ;
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