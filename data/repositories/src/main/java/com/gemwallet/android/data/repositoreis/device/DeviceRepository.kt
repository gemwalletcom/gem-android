package com.gemwallet.android.data.repositoreis.device

import com.gemwallet.android.data.repositoreis.BuildConfig
import com.gemwallet.android.data.repositoreis.config.OfflineFirstConfigRepository.Keys
import com.gemwallet.android.data.service.store.ConfigStore
import java.util.UUID

class DeviceRepository(
    private val configStore: ConfigStore,
) {

    init {
        if (configStore.getString(ConfigKey.DeviceId.string).isEmpty()) {
            configStore.putString(
                Keys.DeviceId.string,
                UUID.randomUUID().toString().substring(0, 31)
            )
        }
    }

    fun getDeviceId(): String = if (BuildConfig.DEBUG) {
        "android_debug_device_id"
    } else {
        configStore.getString(Keys.DeviceId.string)
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

    private enum class ConfigKey(val string: String) {
        DeviceId("device-uuid"),
        ;
    }
}