package com.gemwallet.android.interactors.sync

import com.gemwallet.android.BuildConfig
import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.repositories.session.SessionRepository
import com.gemwallet.android.interactors.SyncOperator
import com.gemwallet.android.services.GemApiClient
import com.gemwallet.android.services.requestPushToken
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.Device
import com.wallet.core.primitives.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.max

class SyncDevice(
    private val gemApiClient: GemApiClient,
    private val configRepository: ConfigRepository,
    private val sessionRepository: SessionRepository,
) : SyncOperator {

    override suspend fun invoke() {
        val pushEnabled = configRepository.pushEnabled()
        val pushToken = if (pushEnabled) configRepository.getPushToken() else ""
        val deviceId = configRepository.getDeviceId()
        val device = Device(
            id = deviceId,
            platform = Platform.Android,
            token = "",
            locale = getLocale(Locale.getDefault()),
            isPushEnabled = pushEnabled,
            version = BuildConfig.VERSION_NAME,
            currency = (sessionRepository.getSession()?.currency ?: Currency.USD).string,
            subscriptionsVersion = configRepository.getSubscriptionVersion(),
        )
        val register: () -> Unit = {
            CoroutineScope(Dispatchers.IO).launch {
                callRegisterDevice(device.copy(token = configRepository.getPushToken()))
            }
        }
        if (pushEnabled && pushToken.isEmpty()) {
            requestPushToken {
                configRepository.setPushToken(it)
                register()
            }
        } else {
            register()
        }
    }

    private suspend fun callRegisterDevice(device: Device) {
        val remoteDeviceInfo = gemApiClient.getDevice(device.id).getOrNull()
        if (remoteDeviceInfo == null) {
            gemApiClient.registerDevice(device)
        } else if (remoteDeviceInfo.hasChanges(device)) {
            val subVersion = max(device.subscriptionsVersion, remoteDeviceInfo.subscriptionsVersion) + 1
            configRepository.setSubscriptionVersion(subVersion)
            gemApiClient.updateDevice(device.id, device.copy(subscriptionsVersion = subVersion))
        }
    }

    private fun Device.hasChanges(other: Device): Boolean {
        return id != other.id
                || token != other.token
                || locale != other.locale
                || isPushEnabled != other.isPushEnabled
                || version != other.version
                || subscriptionsVersion != other.subscriptionsVersion
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