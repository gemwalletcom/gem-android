package com.gemwallet.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RecentType {
    @SerialName("Send")
    Send,
    @SerialName("Receive")
    Receive,
    @SerialName("Buy")
    Buy,
    @SerialName("Swap")
    Swap,
}