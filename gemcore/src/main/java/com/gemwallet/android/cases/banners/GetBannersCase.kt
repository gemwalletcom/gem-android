package com.gemwallet.android.cases.banners

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.Banner
import com.wallet.core.primitives.Wallet

interface GetBannersCase {
    suspend fun getActiveBanners(wallet: Wallet?, asset: Asset?): List<Banner>
}