package com.gemwallet.android.data.repositoreis.device

import com.gemwallet.android.cases.device.GetDeviceIdCase
import com.gemwallet.android.data.repositoreis.config.UserConfig.Keys
import com.gemwallet.android.data.repositoreis.device.DeviceRepository.ConfigKey
import com.gemwallet.android.data.service.store.ConfigStore
import java.util.UUID

class GetDeviceId(
    private val configStore: ConfigStore,
) : GetDeviceIdCase {

    init {
        initDeviceId()
    }

    override fun getDeviceId(): String = configStore.getString(Keys.DeviceId.string)

    private fun initDeviceId() {
        if (configStore.getString(ConfigKey.DeviceId.string).isNotEmpty()) {
            return
        }
        configStore.putString(Keys.DeviceId.string, UUID.randomUUID().toString().substring(0, 31))
    }
}