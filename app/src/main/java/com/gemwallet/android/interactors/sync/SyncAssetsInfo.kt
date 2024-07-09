package com.gemwallet.android.interactors.sync

import com.gemwallet.android.data.asset.AssetsRepository
import com.gemwallet.android.interactors.SyncOperator
import javax.inject.Inject

class SyncAssetsInfo @Inject constructor(
    private val assetsRepository: AssetsRepository,
) : SyncOperator {

    override suspend fun invoke() {

    }
}