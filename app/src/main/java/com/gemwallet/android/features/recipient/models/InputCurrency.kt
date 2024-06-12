package com.gemwallet.android.features.recipient.models

import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.Crypto
import com.gemwallet.android.model.Fiat

enum class InputCurrency {

    InCrypto {

        override fun getAmount(value: String, decimals: Int, price: Double): Crypto =
            Crypto(value.numberParse(), decimals)

        override fun getInput(amount: Crypto?, decimals: Int, price: Double): String
            = amount?.value(decimals)?.stripTrailingZeros()?.toPlainString() ?: ""
    },

    InFiat {
        override fun getAmount(value: String, decimals: Int, price: Double): Crypto =
            Fiat(value.numberParse()).convert(decimals, price)

        override fun getInput(amount: Crypto?, decimals: Int, price: Double): String =
            amount?.convert(decimals, price)
                ?.value(decimals)?.stripTrailingZeros()?.toPlainString()
                ?: ""
    };

    abstract fun getAmount(value: String, decimals: Int, price: Double = 0.0): Crypto

    abstract fun getInput(amount: Crypto?, decimals: Int, price: Double): String
}