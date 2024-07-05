package com.gemwallet.android.ext

import com.wallet.core.primitives.Chain
import com.wallet.core.primitives.EVMChain

fun Chain.toEVM(): EVMChain? {
    return EVMChain.entries.firstOrNull { it.string == string }
}