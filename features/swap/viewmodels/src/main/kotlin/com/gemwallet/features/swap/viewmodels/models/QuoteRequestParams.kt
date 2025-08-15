package com.gemwallet.features.swap.viewmodels.models

import com.gemwallet.android.math.numberParse
import com.gemwallet.android.model.AssetInfo
import java.math.BigDecimal

internal data class QuoteRequestParams(
    val value: BigDecimal,
    val pay: AssetInfo,
    val receive: AssetInfo,
) {
    companion object
}

internal fun QuoteRequestParams.Companion.create(value: String, pay: AssetInfo?, receive: AssetInfo?): QuoteRequestParams? {
    val value = try {
        value.numberParse()
    } catch (_: Throwable) {
        BigDecimal.ZERO
    }
    return if (pay == null || receive == null || pay.id() == receive.id() || value.compareTo(BigDecimal.ZERO) == 0) {
        null
    } else {
        QuoteRequestParams(value, pay, receive)
    }
}