package com.gemwallet.android.domains.perpetual.aggregates

interface PerpetualPositionDetailsDataAggregate : PerpetualPositionDataAggregate {
    val autoClose: String
    val size: String
    val entryPrice: String
    val liquidationPrice: String
    val margin: String
    val fundingPayments: String
}