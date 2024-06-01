package com.gemwallet.android.blockchain.operators

import com.wallet.core.primitives.Chain

interface ValidateAddressOperator {
    operator fun invoke(address: String, chain: Chain): Result<Boolean>
}