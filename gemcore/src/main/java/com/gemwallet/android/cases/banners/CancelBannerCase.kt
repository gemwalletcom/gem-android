package com.gemwallet.android.cases.banners

import com.wallet.core.primitives.Banner

interface CancelBannerCase {
    suspend fun cancelBanner(banner: Banner)
}