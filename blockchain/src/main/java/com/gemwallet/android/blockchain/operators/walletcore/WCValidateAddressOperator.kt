package com.gemwallet.android.blockchain.operators.walletcore

import com.gemwallet.android.blockchain.operators.ValidateAddressOperator
import com.wallet.core.primitives.Chain
import wallet.core.jni.AnyAddress

class WCValidateAddressOperator constructor() : ValidateAddressOperator {
    override operator fun invoke(address: String, chain: Chain): Result<Boolean>
       = Result.success(AnyAddress.isValid(address, WCChainTypeProxy().invoke(chain)))
}