package com.gemwallet.features.bridge.viewmodels.model

import com.reown.walletkit.client.Wallet
import uniffi.gemstone.WalletConnectionVerificationStatus

fun Wallet.Model.VerifyContext.map(): WalletConnectionVerificationStatus {
    if (isScam == true) return WalletConnectionVerificationStatus.MALICIOUS

    return when (this.validation) {
        Wallet.Model.Validation.VALID -> WalletConnectionVerificationStatus.VERIFIED
        Wallet.Model.Validation.INVALID -> WalletConnectionVerificationStatus.INVALID
        Wallet.Model.Validation.UNKNOWN -> WalletConnectionVerificationStatus.UNKNOWN
    }
}