package com.gemwallet.android.application

import com.wallet.core.primitives.AuthPayload
import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.Wallet

interface GetAuthPayload {
    suspend fun getAuthPayload(wallet: Wallet, chain: Chain): AuthPayload
}