package com.gemwallet.android.data.repositoreis.banners

import com.gemwallet.android.cases.banners.AddBannerCase
import com.gemwallet.android.cases.banners.CancelBannerCase
import com.gemwallet.android.cases.banners.GetBannersCase
import com.gemwallet.android.data.repositoreis.config.UserConfig
import com.gemwallet.android.data.service.store.database.BannersDao
import com.gemwallet.android.data.service.store.database.entities.DbBanner
import com.gemwallet.android.data.service.store.database.entities.toModel
import com.gemwallet.android.data.service.store.database.entities.toRecord
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
    private val userConfig: UserConfig
) : GetBannersCase, CancelBannerCase, AddBannerCase {

    override suspend fun getActiveBanners(wallet: Wallet?, asset: Asset?): List<Banner> = withContext(Dispatchers.IO) {
        val generatedBanner = generateBanners(wallet, asset)

        val banners = bannersDao.getBanner(
            walletId = wallet?.id ?: "",
            assetId = asset?.id?.toIdentifier() ?: "",
            chain = asset?.id?.chain,
        ).map { it.toModel(wallet, asset) } + generatedBanner
        banners.filterNotNull().mapNotNull { banner ->
            if (isBannerAvailable(wallet, asset, banner.event)) banner else null
        }
    }

    override suspend fun cancelBanner(banner: Banner) = withContext(Dispatchers.IO) {
        bannersDao.saveBanner(banner.toRecord(BannerState.Cancelled))
    }

    override suspend fun addBanner(
        wallet: Wallet?,
        asset: Asset?,
        chain: Chain?,
        event: BannerEvent,
        state: BannerState,
    ) {
        val banner = DbBanner(
            walletId = wallet?.id ?: "",
            assetId = asset?.id?.toIdentifier() ?: "",
            chain = chain,
            event = event,
            state = state,
        )
        bannersDao.saveBanner(banner)
    }

    private fun generateBanners(wallet: Wallet?, asset: Asset?): Banner? {
        return Banner(
            wallet = wallet,
            asset = asset,
            chain = null,
            state = BannerState.Active,
            event = when {
                wallet == null && asset == null -> BannerEvent.EnableNotifications
//                asset?.id?.chain?.getReserveBalance()?.let { it != BigInteger.ZERO } == true -> BannerEvent.AccountActivation
                asset?.isStackable() == true -> BannerEvent.Stake
                else -> return null
            }
        )
    }

    private suspend fun isBannerAvailable(wallet: Wallet?, asset: Asset?, event: BannerEvent): Boolean {
        if (event == BannerEvent.EnableNotifications && userConfig.getLaunchNumber() < 3) {
            return false
        }
        val dbBanner = bannersDao.getBanner(wallet?.id ?: "", asset?.id?.toIdentifier() ?: "", asset?.id?.chain?.string, event)
        return dbBanner == null || dbBanner.state != BannerState.Cancelled
    }
}