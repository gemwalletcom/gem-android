package com.gemwallet.android.cases.banners

import com.wallet.core.primitives.Asset
import com.wallet.core.primitives.BannerEvent
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet

interface AddBannerCase {
    suspend fun addBanner(wallet: Wallet? = null, asset: Asset? = null, chain: Chain?, event: BannerEvent)
}