package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.GetPerpetual
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualDetailsDataAggregate
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PerpetualData
import com.wallet.core.primitives.PerpetualProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPerpetualImpl @Inject constructor(
    private val perpetualRepository: PerpetualRepository
) : GetPerpetual {

    override fun getPerpetual(perpetualId: String): Flow<PerpetualDetailsDataAggregate?> {
        return perpetualRepository.getPerpetual(perpetualId).map {
            PerpetualDetailsDataAggregateImpl(it ?: return@map null)
        }
    }
}

class PerpetualDetailsDataAggregateImpl(
    val data: PerpetualData
) : PerpetualDetailsDataAggregate {
    override val id: String = data.perpetual.id

    override val provider: PerpetualProvider = data.perpetual.provider

    override val asset: Asset = data.asset

    override val name: String = data.perpetual.name

    override val dayVolume: String = Currency.USD.format(data.perpetual.volume24h)

    override val openInterest: String = Currency.USD.format(data.perpetual.openInterest)

    override val funding: String = Currency.USD.format(data.perpetual.funding)

    override val maxLeverage: Int = data.perpetual.maxLeverage.toInt()

    override val price: Double = data.perpetual.price

    override val identifier: String = data.perpetual.identifier
}