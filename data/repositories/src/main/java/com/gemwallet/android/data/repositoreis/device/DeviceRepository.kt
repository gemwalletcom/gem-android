package com.gemwallet.android.data.repositoreis.device

import android.util.Log
import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.cases.device.GetPushEnabledCase
import com.gemwallet.android.cases.device.GetPushTokenCase
import com.gemwallet.android.cases.device.RequestPushToken
import com.gemwallet.android.cases.device.SetPushTokenCase
import com.gemwallet.android.cases.device.SwitchPushEnabledCase
import com.gemwallet.android.cases.device.SyncDeviceInfoCase
import com.gemwallet.android.cases.device.SyncSubscriptionCase
import com.gemwallet.android.cases.pricealerts.EnablePriceAlertCase
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
import java.util.Locale
import kotlin.math.max

class DeviceRepository(
    private val gemApiClient: GemApiClient,
    private val configStore: ConfigStore,
    private val requestPushToken: RequestPushToken,
    private val platformStore: PlatformStore,
    private val versionName: String,
    private val getDeviceIdCase: GetDeviceIdCase,
    private val enablePriceAlertCase: EnablePriceAlertCase,
    private val getCurrentCurrencyCase: GetCurrentCurrencyCase,
    private val coroutineScope: CoroutineScope = CoroutineScope(
        SupervisorJob() + CoroutineExceptionHandler { _, err -> Log.e("DEVICE", "Err:", err) } + Dispatchers.IO
    ),
) : SyncDeviceInfoCase,
    SwitchPushEnabledCase,
    GetPushEnabledCase,
    GetPushTokenCase,
    SetPushTokenCase,
    SyncSubscriptionCase
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
            isPriceAlertsEnabled = enablePriceAlertCase.isPriceAlertEnabled(),
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
        val remoteDeviceInfo = gemApiClient.getDevice(device.id).getOrNull()
        when {
            remoteDeviceInfo == null -> gemApiClient.registerDevice(device)
            remoteDeviceInfo.hasChanges(device) -> {
                val subVersion = max(device.subscriptionsVersion, remoteDeviceInfo.subscriptionsVersion) + 1
                setSubscriptionVersion(subVersion)
                gemApiClient.updateDevice(device.id, device.copy(subscriptionsVersion = subVersion))
            }
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
        val subscriptionsIndex = mutableMapOf<String, Subscription>()

        wallets.forEach { wallet ->
            wallet.accounts.forEach { account ->
                subscriptionsIndex["${account.chain.string}_${account.address}_${wallet.index}"] = Subscription(
                    chain = account.chain,
                    address = account.address,
                    wallet_index = wallet.index,
                )
            }
        }

        val result = gemApiClient.getSubscriptions(deviceId)
        val remoteSubscriptions = result.getOrNull() ?: emptyList()
        remoteSubscriptions.forEach {
            subscriptionsIndex.remove("${it.chain.string}_${it.address}_${it.wallet_index}")
        }
        if (subscriptionsIndex.isNotEmpty()) {
            gemApiClient.addSubscriptions(deviceId, subscriptionsIndex.values.toList())
            increaseSubscriptionVersion()
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