package com.gemwallet.features.confirm.models

import com.wallet.core.primitives.Asset

data class TxUIModel(
    val from: String,
    val destination: DestinationUIModel?,
    val memo: String?,
    val asset: Asset,
)