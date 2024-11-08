package com.gemwallet.android.data.service.store.database.mappers

import com.gemwallet.android.data.service.store.database.entities.DbBanner
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Banner
import com.wallet.core.primitives.Wallet

class BannerMapper : Mapper<DbBanner, Banner, Pair<Wallet?, Asset?>, Nothing> {
    override fun asDomain(entity: DbBanner, options: (() -> Pair<Wallet?, Asset?>)?): Banner {
        val options = options?.invoke()
        return Banner(
            wallet = options?.first,
            asset = options?.second,
            chain = entity.chain,
            state = entity.state,
            event = entity.event,
        )
    }

    override fun asEntity(domain: Banner, options: (() -> Nothing)?): DbBanner {
        return DbBanner(
            walletId = domain.wallet?.id ?: "",
            assetId = domain.asset?.id?.toIdentifier() ?: "",
            chain = domain.chain,
            event = domain.event,
            state = domain.state,
        )
    }
}