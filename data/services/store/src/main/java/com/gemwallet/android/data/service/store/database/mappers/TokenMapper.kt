package com.gemwallet.android.data.service.store.database.mappers

import com.gemwallet.android.data.service.store.database.entities.DbToken
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset

class TokenMapper : Mapper<Asset, DbToken, Nothing, Nothing> {
    override fun asDomain(entity: Asset, options: (() -> Nothing)?): DbToken {
        return DbToken(
            id = entity.id.toIdentifier(),
            name = entity.name,
            symbol = entity.symbol,
            decimals = entity.decimals,
            type = entity.type,
            rank = 0,
        )
    }

    override fun asEntity(domain: DbToken, options: (() -> Nothing)?): Asset {
        return Asset(
            id = domain.id.toAssetId() ?: throw IllegalArgumentException(),
            name = domain.name,
            symbol = domain.symbol,
            decimals = domain.decimals,
            type = domain.type,
        )
    }
}