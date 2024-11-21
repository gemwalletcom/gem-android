package com.gemwallet.android.cases.swap

import com.wallet.core.primitives.Chain

interface GetSwapSupportChainsCase {
    fun getSwapSupportChains(): List<Chain>
}