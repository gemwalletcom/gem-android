package com.gemwallet.android.domains.referral.values

sealed class ReferralError(message: String = "") : Exception(message) {
    object UnknownError : ReferralError()

    object NetworkError : ReferralError()

    class OperationError(message: String) : ReferralError(message)

    object BadWallet : ReferralError("Bad Wallet")

    object InsufficientPoints : ReferralError()

    object NotCreated : ReferralError()
}