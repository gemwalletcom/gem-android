package com.gemwallet.features.buy.viewmodels.models

import com.gemwallet.android.math.parseNumber

internal class AmountValidator(private val minValue: Double) {
    var error: BuyError? = null
        private set

    fun validate(input: String): Boolean {
        error = null
        val value = try {
            input.ifEmpty { "0.0" }.parseNumber().toDouble()
        } catch (_: Throwable) {
            error = BuyError.ValueIncorrect
            return false
        }
        if (value < minValue) {
            error = BuyError.MinimumAmount
            return false
        }
        if (value == 0.0) {
            error = BuyError.EmptyAmount
            return false
        }
        return true
    }
}