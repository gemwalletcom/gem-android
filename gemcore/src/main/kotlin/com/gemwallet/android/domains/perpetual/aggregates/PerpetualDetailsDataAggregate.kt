package com.gemwallet.android.domains.perpetual.aggregates

interface PerpetualDetailsDataAggregate {
    val name: String
    val dayVolume: String
    val openInterest: String
    val funding: String

}