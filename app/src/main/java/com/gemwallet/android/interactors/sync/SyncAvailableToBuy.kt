package com.gemwallet.android.interactors.sync

import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.interactors.SyncOperator
import com.gemwallet.android.services.GemApiClient

class SyncAvailableToBuy(
    private val gemApiClient: GemApiClient,
    private val configRepository: ConfigRepository,
) : SyncOperator {

    override suspend fun invoke() {
        val assets = configRepository.getFiatAssets()
        if (assets.version.toInt() > 0 && configRepository.getFiatAssetsVersion() <= assets.version.toInt()) {
            return
        }
        gemApiClient.getFiatAssets().mapCatching { configRepository.setFiatAssets(it) }
    }
}