package com.gemwallet.features.settings.price_alerts.viewmodels.models

import java.lang.Exception

sealed class PriceAlertTargetError : Exception() {
    object Zero : PriceAlertTargetError()
    object NotNumber : PriceAlertTargetError()
}