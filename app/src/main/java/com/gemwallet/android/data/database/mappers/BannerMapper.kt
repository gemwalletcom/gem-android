package com.gemwallet.android.data.database.mappers

import com.gemwallet.android.data.database.entities.DbBanner
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Banner

class BannerMapper : Mapper<DbBanner, Banner> {
    override fun asDomain(entity: DbBanner): Banner {
        throw IllegalAccessError()
    }

    override fun asEntity(domain: Banner): DbBanner {
        return DbBanner(
            walletId = domain.wallet?.id ?: "",
            assetId = domain.asset?.id?.toIdentifier() ?: "",
            event = domain.event,
            state = domain.state,
        )
    }
}