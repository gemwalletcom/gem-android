package com.gemwallet.android.data.banners

import com.wallet.core.primitives.Banner

interface CancelBannerCase {
    suspend fun cancelBanner(banner: Banner)
}