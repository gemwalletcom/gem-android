package com.gemwallet.android.cases.banners

import com.wallet.core.primitives.Wallet
import kotlinx.coroutines.flow.Flow

interface HasMultiSign {
    fun hasMultiSign(wallet: Wallet): Flow<Boolean>
}