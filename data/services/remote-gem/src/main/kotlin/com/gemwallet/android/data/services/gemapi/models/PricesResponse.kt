package com.gemwallet.android.data.services.gemapi.models

import com.wallet.core.primitives.AssetPrice
import kotlinx.serialization.Serializable

@Serializable
data class PricesResponse(
    val currency: String,
    val prices: List<AssetPrice> = emptyList()
)