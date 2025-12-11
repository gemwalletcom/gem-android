package com.gemwallet.android.data.coordinates.perpetuals

import com.gemwallet.android.application.perpetual.coordinators.GetPerpetualPosition
import com.gemwallet.android.data.repositoreis.perpetual.PerpetualRepository
import com.gemwallet.android.domains.percentage.formatAsPercentage
import com.gemwallet.android.domains.perpetual.aggregates.PerpetualPositionDetailsDataAggregate
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

class GetPerpetualPositionImpl @Inject constructor(
    private val perpetualRepository: PerpetualRepository
) : GetPerpetualPosition {
    override fun getPositionByPerpetual(id: String): Flow<PerpetualPositionDetailsDataAggregate?> {
        return perpetualRepository.getPositionByPerpetualId(id).map { PerpetualPositionDetailsDataAggregateImpl(it ?: return@map null) }
    }
}

class PerpetualPositionDetailsDataAggregateImpl(
    private val data: PerpetualPositionData,
) : PerpetualPositionDetailsDataAggregate {
    override val autoClose: String = data.position.stopLoss?.price?.let { Currency.USD.format(it) } ?: "-"

    override val size: String = Currency.USD.format(data.position.size)

    override val entryPrice: String = data.position.entryPrice?.let { Currency.USD.format(it) } ?: ""

    override val liquidationPrice: String = data.position.liquidationPrice?.let {  Currency.USD.format(it) } ?: ""

    override val margin: String = "${Currency.USD.format(data.position.marginAmount)} ${data.position.marginType.string}"

    override val fundingPayments: String = Currency.USD.format(data.position.funding?.toDouble() ?: 0.0)

    override val positionId: String = data.position.id

    override val perpetualId: String = data.position.perpetualId

    override val asset: Asset = data.asset

    override val name: String = data.perpetual.name

    override val direction: PerpetualDirection = data.position.direction

    override val leverage: Int = data.position.leverage.toInt()

    override val marginAmount: String = Currency.USD.format(data.position.marginAmount)

    override val pnlWithPercentage: String // TODO: Duplicated
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
    override val pnlState: PriceState  // TODO: Duplicated
        get() = if (data.position.pnl == 0.0) {
            PriceState.None
        } else if (data.position.pnl > 0) {
            PriceState.Up
        } else {
            PriceState.Down
        }
}