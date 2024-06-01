package com.gemwallet.android.interactors.sync

import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.interactors.SyncOperator
import com.gemwallet.android.services.GemApiClient

class SyncNodes(
    private val gemApiClient: GemApiClient,
    private val configRepository: ConfigRepository,
) : SyncOperator {

    override suspend fun invoke(): Result<Boolean> {
        if (configRepository.nodesActual()) {
            return Result.success(true)
        }
        return gemApiClient.getNodes().mapCatching {
            configRepository.setNodes(it.version, it.nodes)
            true
        }
    }
}