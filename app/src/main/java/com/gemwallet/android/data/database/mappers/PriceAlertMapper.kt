package com.gemwallet.android.data.database.mappers

import com.gemwallet.android.data.database.entities.DbPriceAlert
import com.wallet.core.primitives.PriceAlert

class PriceAlertMapper : Mapper<PriceAlert, DbPriceAlert> {
    override fun asDomain(entity: PriceAlert): DbPriceAlert {
        return DbPriceAlert(
            assetId = entity.assetId,
            price = entity.price,
            pricePercentChange = entity.pricePercentChange,
            priceDirection = entity.priceDirection,
            enabled = true,
        )
    }

    override fun asEntity(domain: DbPriceAlert): PriceAlert {
        return PriceAlert(
            assetId = domain.assetId,
            price = domain.price,
            priceDirection = domain.priceDirection,
            pricePercentChange = domain.pricePercentChange,
        )
    }
}