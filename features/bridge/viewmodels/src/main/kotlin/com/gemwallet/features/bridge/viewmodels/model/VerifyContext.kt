package com.gemwallet.features.bridge.viewmodels.model

import com.reown.walletkit.client.Wallet
import uniffi.gemstone.WalletConnectionVerificationStatus

fun Wallet.Model.Validation.map(): WalletConnectionVerificationStatus {
    return when (this) {
        Wallet.Model.Validation.VALID -> WalletConnectionVerificationStatus.VERIFIED
        Wallet.Model.Validation.INVALID -> WalletConnectionVerificationStatus.INVALID
        Wallet.Model.Validation.UNKNOWN -> WalletConnectionVerificationStatus.UNKNOWN
    }
}