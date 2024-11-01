package com.gemwallet.android.data.services.gemapi.models

data class SwapRequest(
    val fromAsset: String,
    val toAsset: String,
    val walletAddress: String,
    val amount: String,
    val includeData: Boolean,
)