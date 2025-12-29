package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualBalance
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.PerpetualBalance
import kotlinx.coroutines.flow.Flow

class GetPerpetualBalanceImpl(
    private val perpetualRepository: PerpetualRepository,
) : GetPerpetualBalance {
    override fun getBalance(
        chain: Chain,
        accountAddress: String
    ): Flow<PerpetualBalance?> = perpetualRepository.getBalance(accountAddress)
}