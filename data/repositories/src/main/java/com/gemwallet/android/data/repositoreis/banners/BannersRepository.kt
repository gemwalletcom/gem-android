package com.gemwallet.android.data.repositoreis.banners

import com.gemwallet.android.cases.banners.CancelBannerCase
import com.gemwallet.android.cases.banners.GetBannersCase
import com.gemwallet.android.data.repositoreis.config.ConfigRepository
import com.gemwallet.android.data.service.store.database.BannersDao
import com.gemwallet.android.data.service.store.database.entities.DbBanner
import com.gemwallet.android.ext.asset
import com.gemwallet.android.ext.isStackable
import com.gemwallet.android.ext.toIdentifier
import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Banner
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.BannerState
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BannersRepository(
    private val bannersDao: BannersDao,
    private val configRepository: ConfigRepository
) : GetBannersCase, CancelBannerCase {

    override suspend fun getActiveBanners(wallet: Wallet?, asset: Asset?): Banner? = withContext(Dispatchers.IO) {
        val event = when {
            wallet == null && asset == null -> BannerEvent.EnableNotifications
            asset?.id?.toIdentifier() == Chain.Xrp.asset().id.toIdentifier() -> BannerEvent.AccountActivation
            asset?.isStackable() == true -> BannerEvent.Stake
            else -> return@withContext null
        }
        if (isBannerAvailable(wallet, asset, event)) {
            Banner(
                wallet = wallet,
                asset = asset,
                event = event,
                state = BannerState.Active,
            )
        } else {
            null
        }
    }

    override suspend fun cancelBanner(banner: Banner) = withContext(Dispatchers.IO) {
        bannersDao.saveBanner(
            DbBanner(
                walletId = banner.wallet?.id ?: "",
                assetId = banner.asset?.id?.toIdentifier() ?: "",
                event = banner.event,
                state = BannerState.Cancelled,
            )
        )
    }

    private suspend fun isBannerAvailable(wallet: Wallet?, asset: Asset?, event: BannerEvent): Boolean {
        if (event == BannerEvent.EnableNotifications && configRepository.getLaunchNumber() < 3) {
            return false
        }
        val dbBanner = bannersDao.getBanner(wallet?.id ?: "", asset?.id?.toIdentifier() ?: "", event)
        return dbBanner == null || dbBanner.state != BannerState.Cancelled
    }
}