package com.gemwallet.android.blockchain.operators

import com.wallet.core.primitives.Chain

interface ChainTypeProxy<T> {
    operator fun  invoke(chain: Chain): T
}