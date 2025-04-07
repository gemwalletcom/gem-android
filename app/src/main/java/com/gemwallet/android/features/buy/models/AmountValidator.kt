package com.gemwallet.android.features.buy.models

import com.gemwallet.android.math.numberParse

internal class AmountValidator(private val minValue: Double) {
    var error: BuyError? = null
        private set

    fun validate(input: String): Boolean {
        error = null
        val value = try {
            input.ifEmpty { "0.0" }.numberParse().toDouble()
        } catch (_: Throwable) {
            BuyError.ValueIncorrect.also { error = it }
            return false
        }
        if (value < minValue) {
            BuyError.MinimumAmount.also { error = it }
            return false
        }
        return true
    }
}