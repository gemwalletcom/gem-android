package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.SyncPerpetuals
import com.gemwallet.android.blockchain.services.PerpetualService
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.wallet.core.primitives.Chain
import javax.inject.Inject

class SyncPerpetualsImpl @Inject constructor(
    private val perpetualService: PerpetualService,
    private val perpetualRepository: PerpetualRepository,
    private val chains: List<Chain>,
) : SyncPerpetuals {

    override suspend fun syncPerpetuals() {
        chains.map { chain ->
            perpetualService.getPerpetualsData(chain = chain)
        }.map {
            perpetualRepository.removeNotAvailablePerpetuals(it)
            perpetualRepository.putPerpetuals(it)
        }
    }
}