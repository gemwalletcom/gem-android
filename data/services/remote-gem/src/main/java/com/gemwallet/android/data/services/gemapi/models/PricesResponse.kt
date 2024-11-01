package com.gemwallet.android.data.services.gemapi.models

import com.wallet.core.primitives.AssetPrice

data class PricesResponse(
    val currency: String,
    val prices: List<AssetPrice> = emptyList()
)