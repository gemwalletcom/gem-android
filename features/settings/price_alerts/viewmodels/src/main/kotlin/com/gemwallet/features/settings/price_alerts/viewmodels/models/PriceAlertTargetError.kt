package com.gemwallet.features.settings.price_alerts.viewmodels.models

sealed class PriceAlertTargetError : Exception() {
    object Zero : PriceAlertTargetError()
    object NotNumber : PriceAlertTargetError()
}