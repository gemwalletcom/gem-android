package com.gemwallet.features.swap.viewmodels.models

import kotlinx.serialization.Serializable

@Serializable
enum class SwapItemType {
    Pay,
    Receive,
}