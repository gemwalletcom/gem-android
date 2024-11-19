package com.gemwallet.android.data.repositoreis.swap

import com.gemwallet.android.cases.swap.GetSwapSupportChainsCase
import com.wallet.core.primitives.Chain
import uniffi.gemstone.GemSwapper

class GetSwapSupportChains(
    private val swapper: GemSwapper,
) : GetSwapSupportChainsCase {

    override fun getSwapSupportChains(): List<Chain> = swapper.supportedChains()
        .mapNotNull {
            Chain.entries.firstOrNull { chain -> chain.string == it }
        }
}