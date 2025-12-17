package com.gemwallet.android.data.coordinates.referral

sealed class ReferralError(message: String = "") : Exception(message) {
    object BadWallet : ReferralError("Bad Wallet")
}