package com.gemwallet.android.interactors.sync

import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.interactors.SyncOperator
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.ConfigResponse

class SyncConfig(
    private val gemApiClient: GemApiClient,
    private val configRepository: ConfigRepository,
) : SyncOperator {
    override suspend fun invoke() {
        gemApiClient.getConfig().mapCatching { saveConfig(it) }
    }

    private fun saveConfig(config: ConfigResponse) {
        with(configRepository) {
            setFiatAssetsVersion(config.versions.fiatAssets)
        }

    }
}