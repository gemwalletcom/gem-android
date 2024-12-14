package com.gemwallet.android.ext

import com.wallet.core.primitives.BitcoinChain
import com.wallet.core.primitives.Chain

fun Chain.toBitcoinChain() = when (this) {
    Chain.Bitcoin -> BitcoinChain.Bitcoin
    Chain.Doge -> BitcoinChain.Doge
    Chain.Litecoin -> BitcoinChain.Litecoin
    else -> throw IllegalArgumentException("Not bitcoin chain")
}