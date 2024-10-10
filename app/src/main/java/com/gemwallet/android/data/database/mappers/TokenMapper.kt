package com.gemwallet.android.data.database.mappers

import com.gemwallet.android.data.database.entities.DbToken
import com.gemwallet.android.ext.toAssetId
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset

class TokenMapper : Mapper<Asset, DbToken> {
    override fun asDomain(entity: Asset): DbToken {
        return DbToken(
            id = entity.id.toIdentifier(),
            name = entity.name,
            symbol = entity.symbol,
            decimals = entity.decimals,
            type = entity.type,
            rank = 0,
        )
    }

    override fun asEntity(domain: DbToken): Asset {
        return Asset(
            id = domain.id.toAssetId() ?: throw IllegalArgumentException(),
            name = domain.name,
            symbol = domain.symbol,
            decimals = domain.decimals,
            type = domain.type,
        )
    }
}