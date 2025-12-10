package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.GetPerpetuals
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.gemwallet.android.domains.price.values.PriceableValue
import com.gemwallet.android.model.compactFormatter
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PerpetualData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPerpetualsImpl @Inject constructor(
    private val perpetualRepository: PerpetualRepository,
) : GetPerpetuals {

    override fun getPerpetuals(searchQuery: String?): Flow<List<PerpetualDataAggregate>> {
        return perpetualRepository.getPerpetuals(searchQuery)
            .map { items -> items.map { PerpetualDataAggregate(it) } }
    }

    class PerpetualDataAggregate(
        val data: PerpetualData,
        override val price: PriceableValue = object : PriceableValue { // TODO: ???
            override val priceValue: Double = data.perpetual.price
            override val currency: Currency = Currency.USD
            override val dayChangePercentage: Double = data.perpetual.pricePercentChange24h
        }
    ) : com.gemwallet.android.domains.perpetual.aggregates.PerpetualDataAggregate {

        override val id: String = data.perpetual.id

        override val asset: Asset = data.asset

        override val name: String = data.perpetual.name

        override val icon: Any = data.asset

        override val volume: String = price.currency.compactFormatter(data.perpetual.volume24h)
    }
}