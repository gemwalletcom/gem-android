package com.gemwallet.android.data.repositoreis.device

import com.gemwallet.android.cases.device.GetDeviceIdOld
import com.gemwallet.android.data.repositoreis.config.UserConfig.Keys
import com.gemwallet.android.data.repositoreis.device.DeviceRepository.ConfigKey
import com.gemwallet.android.data.service.store.ConfigStore
import java.util.UUID

class GetDeviceIdOldImpl(
    private val configStore: ConfigStore,
) : GetDeviceIdOld {

    init {
        initDeviceId()
    }

    override fun getDeviceId(): String = configStore.getString(Keys.DeviceId.string)

    override fun isMigrated(): Boolean = configStore.getBoolean(Keys.DeviceIdMigrated.string)

    override fun migrated() {
        configStore.getBoolean(Keys.DeviceIdMigrated.string, true)
    }

    private fun initDeviceId() {
        if (configStore.getString(ConfigKey.DeviceId.string).isEmpty()) {
            configStore.getBoolean(Keys.DeviceIdMigrated.string, true)
        }
    }
}