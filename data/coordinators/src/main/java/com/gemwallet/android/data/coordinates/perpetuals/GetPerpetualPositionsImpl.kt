package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualPositions
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.gemwallet.android.domains.percentage.formatAsPercentage
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualPositionDataAggregate
import com.gemwallet.android.domains.price.PriceState
import com.gemwallet.android.model.format
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Currency
import com.wallet.core.primitives.PerpetualDirection
import com.wallet.core.primitives.PerpetualPositionData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.absoluteValue

class GetPerpetualPositionsImpl @Inject constructor(
    private val perpetualsRepository: PerpetualRepository,
) : GetPerpetualPositions {

    override fun getPerpetualPositions(): Flow<List<PerpetualPositionDataAggregateImpl>> {
        return perpetualsRepository.getPositions()
            .map { items -> items.map { PerpetualPositionDataAggregateImpl(it) } }
    }
}

class PerpetualPositionDataAggregateImpl(val data: PerpetualPositionData) : PerpetualPositionDataAggregate {
    override val positionId: String = data.position.id
    override val perpetualId: String = data.perpetual.id
    override val asset: Asset = data.asset
    override val name: String = data.perpetual.name
    override val direction: PerpetualDirection = data.position.direction
    override val leverage: Int = data.position.leverage.toInt()
    override val marginAmount: String = Currency.USD.format(data.position.marginAmount)
    override val pnlWithPercentage: String
        get() {
            val percentage = ((data.position.pnl / data.position.marginAmount) * 100).formatAsPercentage()
            val pnl = data.position.pnl.absoluteValue
            val pnlFormatted = Currency.USD.format(pnl)
            return if (data.position.pnl >= 0) {
                "+$pnlFormatted ($percentage)"
            } else {
                "-$pnlFormatted ($percentage)"
            }
        }
    override val pnlState: PriceState
        get() = if (data.position.pnl == 0.0) {
            PriceState.None
        } else if (data.position.pnl > 0) {
            PriceState.Up
        } else {
            PriceState.Down
        }
}