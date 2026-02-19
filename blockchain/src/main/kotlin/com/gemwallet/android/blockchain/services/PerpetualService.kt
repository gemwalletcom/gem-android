package com.gemwallet.android.blockchain.services

import com.gemwallet.android.blockchain.services.mapper.toDTO
import com.gemwallet.android.ext.toAssetId
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.ChartCandleStick
import com.wallet.core.primitives.ChartPeriod
import com.wallet.core.primitives.Perpetual
import com.wallet.core.primitives.PerpetualBalance
import com.wallet.core.primitives.PerpetualData
import com.wallet.core.primitives.PerpetualDirection
import com.wallet.core.primitives.PerpetualMarginType
import com.wallet.core.primitives.PerpetualMetadata
import com.wallet.core.primitives.PerpetualOrderType
import com.wallet.core.primitives.PerpetualPosition
import com.wallet.core.primitives.PerpetualPositionsSummary
import com.wallet.core.primitives.PerpetualProvider
import com.wallet.core.primitives.PerpetualTriggerOrder
import uniffi.gemstone.GemChartCandleStick
import uniffi.gemstone.GemGateway
import uniffi.gemstone.GemPerpetualData
import uniffi.gemstone.GemPerpetualMarginType
import uniffi.gemstone.GemPerpetualOrderType
import uniffi.gemstone.GemPerpetualPosition
import uniffi.gemstone.GemPerpetualPositionsSummary

class PerpetualService(
    private val gateway: GemGateway,
) {
    suspend fun getPerpetualsData(chain: Chain = Chain.HyperCore): List<PerpetualData> {
        val response = try {
            gateway.getPerpetualsData(chain.string)
        } catch (_: Throwable) {
            return emptyList()
        }
        return response.mapNotNull { it.toDTO() }
    }

    suspend fun getPositions(chain: Chain = Chain.HyperCore, address: String): PerpetualPositionsSummary? {
        val response = try {
            gateway.getPositions(chain.string, address)
        } catch (_: Throwable) {
            return null
        }
        return response.toDTO()
    }

    suspend fun getCandleSticks(chain: Chain = Chain.HyperCore, symbol: String, period: ChartPeriod): List<ChartCandleStick> {
        val response = try {
            gateway.getPerpetualCandlesticks(chain.string, symbol, period.string)
        } catch (_: Throwable) {
            return emptyList()
        }
        return response.map { it.toDTO() }
    }
}

fun GemPerpetualData.toDTO(): PerpetualData? {
    return PerpetualData(
        perpetual = Perpetual(
            id = perpetual.id,
            name = perpetual.name,
            provider = perpetual.provider.toDTO(),
            assetId = perpetual.assetId.toAssetId() ?: return null,
            identifier = perpetual.identifier,
            price = perpetual.price,
            pricePercentChange24h = perpetual.pricePercentChange24h,
            openInterest = perpetual.openInterest,
            volume24h = perpetual.volume24h,
            funding = perpetual.funding,
            maxLeverage = perpetual.maxLeverage,
        ),
        asset = asset.toDTO(),
        metadata = PerpetualMetadata(
            isPinned = metadata.isPinned
        ),
    )
}

fun GemPerpetualPositionsSummary.toDTO(): PerpetualPositionsSummary {
    return PerpetualPositionsSummary(
         positions = positions.mapNotNull { it.toDTO() },
         balance = PerpetualBalance(
             available = balance.available,
             reserved = balance.reserved,
             withdrawable = balance.withdrawable,
         ),
    )
}

fun GemPerpetualPosition.toDTO(): PerpetualPosition? {
    return PerpetualPosition(
        id = id,
        perpetualId = perpetualId,
        assetId = assetId.toAssetId() ?: return null,
        size = size,
        sizeValue = sizeValue,
        leverage = leverage,
        entryPrice = entryPrice,
        liquidationPrice = liquidationPrice,
        marginType = when (marginType) {
            GemPerpetualMarginType.CROSS -> PerpetualMarginType.Cross
            GemPerpetualMarginType.ISOLATED -> PerpetualMarginType.Isolated
        },
        direction = when (direction) {
            uniffi.gemstone.PerpetualDirection.SHORT -> PerpetualDirection.Short
            uniffi.gemstone.PerpetualDirection.LONG -> PerpetualDirection.Long
        },
        marginAmount = marginAmount,
        takeProfit = takeProfit?.let { takeProfit -> 
            PerpetualTriggerOrder(
                price = takeProfit.price,
                order_type = when (takeProfit.orderType) {
                    GemPerpetualOrderType.MARKET -> PerpetualOrderType.Market
                    GemPerpetualOrderType.LIMIT -> PerpetualOrderType.Limit
                },
                order_id = takeProfit.orderId
            )
        },
        stopLoss = stopLoss?.let { stopLoss -> 
            PerpetualTriggerOrder(
                price = stopLoss.price,
                order_type = when (stopLoss.orderType) {
                    GemPerpetualOrderType.MARKET -> PerpetualOrderType.Market
                    GemPerpetualOrderType.LIMIT -> PerpetualOrderType.Limit
                },
                order_id = stopLoss.orderId
            )
        },
        pnl = pnl,
        funding = funding,
    )
}

fun GemChartCandleStick.toDTO(): ChartCandleStick {
    return ChartCandleStick(
        date = date,
        open = open,
        high = high,
        low = low,
        close = close,
        volume = volume,
    )
}

fun uniffi.gemstone.PerpetualProvider.toDTO(): PerpetualProvider = when (this) {
    uniffi.gemstone.PerpetualProvider.HYPERCORE -> PerpetualProvider.Hypercore
}