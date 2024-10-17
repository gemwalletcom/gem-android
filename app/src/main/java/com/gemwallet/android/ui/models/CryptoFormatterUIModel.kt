package com.gemwallet.android.ui.models

import com.gemwallet.android.model.format
import com.wallet.core.primitives.Asset

interface CryptoFormatterUIModel : CryptoAmountUIModel, AssetUIModel {

    val cryptoAmount: String
        get() = asset.format(crypto = crypto, decimalPlace = 6)
}