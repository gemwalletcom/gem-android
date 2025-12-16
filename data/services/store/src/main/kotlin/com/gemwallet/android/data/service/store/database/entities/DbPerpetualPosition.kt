package com.gemwallet.android.data.service.store.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Relation
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.PerpetualDirection
import com.wallet.core.primitives.PerpetualMarginType
import com.wallet.core.primitives.PerpetualOrderType
import com.wallet.core.primitives.PerpetualPosition
import com.wallet.core.primitives.PerpetualPositionData
import com.wallet.core.primitives.PerpetualTriggerOrder

@Entity(
    tableName = "perpetual_position",
    primaryKeys = ["id", "perpetualId", "accountAddress"],
    indices = [
        Index(name = "perpetual_position_asset_id_idx", value = ["assetId"]),
        Index(name = "perpetual_position_perpetual_id_idx", value = ["perpetualId"]),
        Index(name = "perpetual_position_account_address_idx", value = ["accountAddress"]),
    ],
)
data class DbPerpetualPosition(
    val id: String,
    val perpetualId: String,
    val accountAddress: String,
    val assetId: String,
    val size: Double,
    val sizeValue: Double,
    val leverage: Int,
    val entryPrice: Double? = null,
    val liquidationPrice: Double? = null,
    val marginType: PerpetualMarginType,
    val direction: PerpetualDirection,
    val marginAmount: Double,
    // Trigger: profit
    val takeProfitPrice: Double? = null,
    val takeProfitType: PerpetualOrderType? = null,
    val takeProfitOrderId: String? = null,
    // // Trigger: stop loss
    val stopLossPrice: Double? = null,
    val stopLossType: PerpetualOrderType? = null,
    val stopLossOrderId: String? = null,
    val pnl: Double,
    val funding: Float? = null,
)

data class DbPerpetualPositionData(

    @Embedded
    val position: DbPerpetualPosition,

    @Relation(
        parentColumn = "perpetualId",
        entityColumn = "id"
    )
    val perpetual: DbPerpetual,


    @Relation(
        parentColumn = "assetId",
        entityColumn = "id"
    )
    val asset: DbPerpetualAsset,
)

fun DbPerpetualPosition.toDto(): PerpetualPosition? {
    val takeProfitTrigger = if (takeProfitType != null && takeProfitPrice != null && takeProfitOrderId != null) {
        PerpetualTriggerOrder(
            price = takeProfitPrice,
            order_type = takeProfitType,
            order_id = takeProfitOrderId,
        )
    } else null

    val stopLossTrigger = if (stopLossType != null && stopLossPrice != null && stopLossOrderId != null) {
        PerpetualTriggerOrder(
            price = stopLossPrice,
            order_type = stopLossType,
            order_id = stopLossOrderId,
        )
    } else null
    
    return PerpetualPosition(
        id = id,
        perpetualId = perpetualId,
        assetId = assetId.toAssetId() ?: return null,
        size = size,
        sizeValue = sizeValue,
        leverage = leverage.toUByte(),
        entryPrice = entryPrice,
        liquidationPrice = liquidationPrice,
        marginType = marginType,
        direction = direction,
        marginAmount = marginAmount,
        takeProfit = takeProfitTrigger,
        stopLoss = stopLossTrigger,
        pnl = pnl,
        funding = funding,
    )
}

fun PerpetualPosition.toDB(accountAddress: String): DbPerpetualPosition {
    return DbPerpetualPosition(
        id = id,
        perpetualId = perpetualId,
        accountAddress = accountAddress,
        assetId = assetId.toIdentifier(),
        size = size,
        sizeValue = sizeValue,
        leverage = leverage.toInt(),
        entryPrice = entryPrice,
        liquidationPrice = liquidationPrice,
        marginType = marginType,
        direction = direction,
        marginAmount = marginAmount,
        takeProfitPrice = takeProfit?.price,
        takeProfitType = takeProfit?.order_type,
        takeProfitOrderId = takeProfit?.order_id,
        stopLossPrice = stopLoss?.price,
        stopLossType = stopLoss?.order_type,
        stopLossOrderId = stopLoss?.order_id,
        pnl = pnl,
        funding = funding,
    )
}

fun DbPerpetualPositionData.toDTO(): PerpetualPositionData? {
    return PerpetualPositionData(
        perpetual = perpetual.toDTO() ?: return null,
        asset = asset.toDTO() ?: return null,
        position = position.toDto() ?: return null
    )
}