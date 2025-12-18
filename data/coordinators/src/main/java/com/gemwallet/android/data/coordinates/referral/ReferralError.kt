package com.gemwallet.android.data.coordinates.referral

sealed class ReferralError(message: String = "") : Exception(message) {
    object UnknownError : ReferralError()

    object NetworkError : ReferralError()

    class OperationError(message: String) : ReferralError(message)

    object BadWallet : ReferralError("Bad Wallet")
}