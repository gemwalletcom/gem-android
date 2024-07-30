package com.gemwallet.android.features.receive.model

import com.wallet.core.primitives.Chain

data class ReceiveScreenModel(
    val walletName: String = "",
    val address: String = "",
    val assetTitle: String = "",
    val assetSymbol: String = "",
    val chain: Chain? = null,
)
