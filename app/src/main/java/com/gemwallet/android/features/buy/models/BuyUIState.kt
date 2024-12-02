package com.gemwallet.android.features.buy.models

import androidx.compose.runtime.Stable
import com.wallet.core.primitives.FiatProvider

@Stable
data class BuyProvider(
    val provider: FiatProvider,
    val cryptoAmount: String,
    val rate: String,
    val redirectUrl: String?,
)

