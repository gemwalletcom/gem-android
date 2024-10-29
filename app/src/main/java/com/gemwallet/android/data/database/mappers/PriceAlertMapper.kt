package com.gemwallet.android.data.database.mappers

import com.gemwallet.android.data.database.entities.DbPriceAlert
import com.wallet.core.primitives.PriceAlert

class PriceAlertMapper : Mapper<PriceAlert, DbPriceAlert, Nothing, Nothing> {

    override fun asDomain(entity: PriceAlert, options: (() -> Nothing)?): DbPriceAlert {
        return DbPriceAlert(
            assetId = entity.assetId,
            price = entity.price,
            pricePercentChange = entity.pricePercentChange,
            priceDirection = entity.priceDirection,
            enabled = true,
        )
    }

    override fun asEntity(domain: DbPriceAlert, options: (() -> Nothing)?): PriceAlert {
        return PriceAlert(
            assetId = domain.assetId,
            price = domain.price,
            priceDirection = domain.priceDirection,
            pricePercentChange = domain.pricePercentChange,
        )
    }
}