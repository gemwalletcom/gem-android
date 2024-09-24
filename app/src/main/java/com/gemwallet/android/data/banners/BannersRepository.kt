package com.gemwallet.android.data.banners

import com.gemwallet.android.data.config.ConfigRepository
import com.gemwallet.android.data.database.BannersDao
import com.gemwallet.android.data.database.entities.DbBanner
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
            wallet == null && asset == null -> BannerEvent.enable_notifications
            asset?.id?.toIdentifier() == Chain.Xrp.asset().id.toIdentifier() -> BannerEvent.account_activation
            asset?.isStackable() == true -> BannerEvent.stake
            else -> return@withContext null
        }
        if (isBannerAvailable(wallet, asset, event)) {
            Banner(
                wallet = wallet,
                asset = asset,
                event = event,
                state = BannerState.active,
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
                state = BannerState.cancelled,
            )
        )
    }

    private suspend fun isBannerAvailable(wallet: Wallet?, asset: Asset?, event: BannerEvent): Boolean {
        if (event == BannerEvent.enable_notifications && configRepository.getLaunchNumber() < 3) {
            return false
        }
        val dbBanner = bannersDao.getBanner(wallet?.id ?: "", asset?.id?.toIdentifier() ?: "", event)
        return dbBanner == null || dbBanner.state != BannerState.cancelled
    }
}