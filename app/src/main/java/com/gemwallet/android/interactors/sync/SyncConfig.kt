package com.gemwallet.android.interactors.sync

import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.interactors.SyncOperator
import com.gemwallet.android.services.GemApiClient
import com.wallet.core.primitives.ConfigResponse

class SyncConfig(
    private val gemApiClient: GemApiClient,
    private val configRepository: ConfigRepository,
) : SyncOperator {
    override suspend fun invoke(): Result<Boolean> {
        return gemApiClient.getConfig().mapCatching {
            saveConfig(it)
            true
        }
    }

    private fun saveConfig(config: ConfigResponse) {
        with(configRepository) {
            setNodesVersion(config.versions.nodes)
            setFiatAssetsVersion(config.versions.fiatAssets)
            setAppVersion(config.app.android.version.alpha, ConfigRepository.AppVersionType.Alpha)
            setAppVersion(config.app.android.version.beta, ConfigRepository.AppVersionType.Beta)
            setAppVersion(config.app.android.version.production, ConfigRepository.AppVersionType.Prod)
        }

    }
}